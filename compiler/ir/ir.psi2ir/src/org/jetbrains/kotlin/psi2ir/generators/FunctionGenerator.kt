/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.util.SKIP_BODIES_ERROR_DESCRIPTION
import org.jetbrains.kotlin.ir.util.declareSimpleFunctionWithOverrides
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.psi2ir.isConstructorDelegatingToSuper
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.isSingleUnderscore
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.descriptorUtil.isAnnotationConstructor
import org.jetbrains.kotlin.resolve.descriptorUtil.isEffectivelyExternal
import org.jetbrains.kotlin.resolve.descriptorUtil.propertyIfAccessor

@ObsoleteDescriptorBasedAPI
internal class FunctionGenerator(declarationGenerator: DeclarationGenerator) : DeclarationGeneratorExtension(declarationGenerator) {

    constructor(context: GeneratorContext) : this(DeclarationGenerator(context))

    @JvmOverloads
    fun generateFunctionDeclaration(
        ktFunction: KtNamedFunction,
        parentLoopResolver: LoopResolver?,
        origin: IrDeclarationOrigin = IrDeclarationOrigin.DEFINED
    ): IrSimpleFunction =
        declareSimpleFunction(
            ktFunction,
            ktFunction.receiverTypeReference,
            ktFunction.contextReceivers.mapNotNull { it.typeReference() },
            origin,
            getOrFail(BindingContext.FUNCTION, ktFunction),
            parentLoopResolver
        ) {
            ktFunction.bodyExpression?.let { generateFunctionBody(it) }
        }

    fun generateLambdaFunctionDeclaration(ktFunction: KtFunctionLiteral, parentLoopResolver: LoopResolver?): IrSimpleFunction {
        val lambdaDescriptor = getOrFail(BindingContext.FUNCTION, ktFunction)
        return declareSimpleFunction(
            ktFunction,
            null,
            emptyList(),
            IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA,
            lambdaDescriptor,
            parentLoopResolver
        ) {
            generateLambdaBody(ktFunction, lambdaDescriptor)
        }
    }

    fun generateFakeOverrideFunction(functionDescriptor: FunctionDescriptor, ktElement: KtPureElement): IrSimpleFunction? =
        functionDescriptor.takeIf { it.visibility != DescriptorVisibilities.INVISIBLE_FAKE }
            ?.let {
                declareSimpleFunctionInner(it, ktElement, IrDeclarationOrigin.FAKE_OVERRIDE)
                    .buildWithScope { irFunction ->
                        generateFunctionParameterDeclarationsAndReturnType(irFunction, ktElement, null, emptyList())
                    }
            }

    private inline fun declareSimpleFunction(
        ktFunction: KtFunction,
        ktReceiver: KtElement?,
        ktContextReceivers: List<KtElement>,
        origin: IrDeclarationOrigin,
        descriptor: FunctionDescriptor,
        parentLoopResolver: LoopResolver?,
        generateBody: BodyGenerator.() -> IrBody?
    ): IrSimpleFunction =
        declareSimpleFunctionInner(descriptor, ktFunction, origin).buildWithScope { irFunction ->
            if (descriptor != irFunction.descriptor) {
                val message = """
                    |IrSymbolTable contains function with wrong descriptor.
                    |Expected: $descriptor
                    |Actual: ${irFunction.descriptor}
                """.trimMargin()
                error(message)
            }
            generateFunctionParameterDeclarationsAndReturnType(irFunction, ktFunction, ktReceiver, ktContextReceivers)
            irFunction.body = createBodyGenerator(irFunction.symbol, parentLoopResolver).generateBody()
        }

    private fun declareSimpleFunctionInner(
        descriptor: FunctionDescriptor,
        ktElement: KtPureElement,
        origin: IrDeclarationOrigin
    ): IrSimpleFunction =
        context.symbolTable.declareSimpleFunctionWithOverrides(
            ktElement.getStartOffsetOfFunctionDeclarationKeywordOrNull() ?: ktElement.pureStartOffset,
            ktElement.pureEndOffset,
            origin,
            descriptor
        )

    fun generateFunctionParameterDeclarationsAndReturnType(
        irFunction: IrFunction,
        ktParameterOwner: KtPureElement?,
        ktReceiverParameterElement: KtElement?,
        ktContextReceiverParametersElements: List<KtElement>
    ) {
        declarationGenerator.generateScopedTypeParameterDeclarations(irFunction, irFunction.descriptor.propertyIfAccessor.typeParameters)
        irFunction.returnType = irFunction.descriptor.returnType!!.toIrType()
        generateValueParameterDeclarations(irFunction, ktParameterOwner, ktReceiverParameterElement, ktContextReceiverParametersElements)
    }

    fun generatePropertyAccessor(
        descriptor: PropertyAccessorDescriptor,
        ktProperty: KtVariableDeclaration,
        ktAccessor: KtPropertyAccessor?
    ): IrSimpleFunction =
        declareSimpleFunctionInner(
            descriptor,
            ktAccessor ?: ktProperty,
            if (ktAccessor != null && ktAccessor.hasBody()) IrDeclarationOrigin.DEFINED else IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
        ).buildWithScope { irAccessor ->
            declarationGenerator.generateScopedTypeParameterDeclarations(irAccessor, descriptor.correspondingProperty.typeParameters)
            irAccessor.returnType = irAccessor.descriptor.returnType!!.toIrType()
            generateValueParameterDeclarations(
                irAccessor, ktAccessor ?: ktProperty, ktProperty.receiverTypeReference,
                ktProperty.contextReceivers.mapNotNull { it.typeReference() }
            )
            val ktBodyExpression = ktAccessor?.bodyExpression
            irAccessor.body =
                if (ktBodyExpression != null)
                    createBodyGenerator(irAccessor.symbol).generateFunctionBody(ktBodyExpression)
                else
                    generateDefaultAccessorBody(descriptor, irAccessor)
        }

    fun generateDefaultAccessorForPrimaryConstructorParameter(
        descriptor: PropertyAccessorDescriptor,
        ktElement: KtElement
    ): IrSimpleFunction =
        declareSimpleFunctionInner(descriptor, ktElement, IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR).buildWithScope { irAccessor ->
            declarationGenerator.generateScopedTypeParameterDeclarations(irAccessor, descriptor.typeParameters)
            irAccessor.returnType = descriptor.returnType!!.toIrType()
            FunctionGenerator(declarationGenerator).generateSyntheticFunctionParameterDeclarations(irAccessor)
            irAccessor.body = generateDefaultAccessorBody(descriptor, irAccessor)
        }

    private fun generateDefaultAccessorBody(
        accessor: PropertyAccessorDescriptor,
        irAccessor: IrSimpleFunction
    ): IrBlockBody? =
        if (accessor.modality == Modality.ABSTRACT || accessor.correspondingProperty.isExpect || context.configuration.skipBodies)
            null
        else
            when (accessor) {
                is PropertyGetterDescriptor -> generateDefaultGetterBody(accessor, irAccessor)
                is PropertySetterDescriptor -> generateDefaultSetterBody(accessor, irAccessor)
                else -> throw AssertionError("Should be getter or setter: $accessor")
            }

    private fun generateDefaultGetterBody(
        getter: PropertyGetterDescriptor,
        irAccessor: IrSimpleFunction
    ): IrBlockBody {
        val property = getter.correspondingProperty

        val startOffset = irAccessor.startOffset
        val endOffset = irAccessor.endOffset
        val irBody = context.irFactory.createBlockBody(startOffset, endOffset)

        val receiver = generateReceiverExpressionForDefaultPropertyAccessor(property, irAccessor)

        irBody.statements.add(
            IrReturnImpl(
                startOffset, endOffset, context.irBuiltIns.nothingType,
                irAccessor.symbol,
                IrGetFieldImpl(
                    startOffset, endOffset,
                    context.symbolTable.descriptorExtension.referenceField(property),
                    property.type.toIrType(),
                    receiver
                )
            )
        )
        return irBody
    }

    private fun generateDefaultSetterBody(
        setter: PropertySetterDescriptor,
        irAccessor: IrSimpleFunction
    ): IrBlockBody {
        val property = setter.correspondingProperty

        val startOffset = irAccessor.startOffset
        val endOffset = irAccessor.endOffset
        val irBody = context.irFactory.createBlockBody(startOffset, endOffset)

        val receiver = generateReceiverExpressionForDefaultPropertyAccessor(property, irAccessor)

        val irValueParameter = irAccessor.parameters.single { it.kind == IrParameterKind.Regular || it.kind == IrParameterKind.Context }
        irBody.statements.add(
            IrSetFieldImpl(
                startOffset, endOffset,
                context.symbolTable.descriptorExtension.referenceField(property),
                receiver,
                IrGetValueImpl(startOffset, endOffset, irValueParameter.type, irValueParameter.symbol),
                context.irBuiltIns.unitType
            )
        )
        return irBody
    }

    private fun generateReceiverExpressionForDefaultPropertyAccessor(
        property: PropertyDescriptor,
        irAccessor: IrSimpleFunction
    ): IrExpression? {
        return when (val containingDeclaration = property.containingDeclaration) {
            is ClassDescriptor -> {
                val thisAsReceiverParameter = containingDeclaration.thisAsReceiverParameter
                IrGetValueImpl(
                    irAccessor.startOffset, irAccessor.endOffset,
                    thisAsReceiverParameter.type.toIrType(),
                    context.symbolTable.descriptorExtension.referenceValue(thisAsReceiverParameter)
                )
            }
            else -> null
        }
    }

    fun generatePrimaryConstructor(
        primaryConstructorDescriptor: ClassConstructorDescriptor,
        ktClassOrObject: KtPureClassOrObject
    ): IrConstructor =
        declareConstructor(
            ktClassOrObject.primaryConstructor ?: ktClassOrObject,
            ktClassOrObject.contextReceivers.mapNotNull { it.typeReference() },
            primaryConstructorDescriptor
        ) { irConstructor ->
            if (
                primaryConstructorDescriptor.isExpect ||
                primaryConstructorDescriptor.constructedClass.isEffectivelyExternal() ||
                context.configuration.skipBodies
            )
                null
            else
                generatePrimaryConstructorBody(ktClassOrObject, irConstructor)
        }

    fun generateSecondaryConstructor(ktConstructor: KtSecondaryConstructor, ktClassOrObject: KtPureClassOrObject): IrConstructor {
        val constructorDescriptor = getOrFail(BindingContext.CONSTRUCTOR, ktConstructor) as ClassConstructorDescriptor
        return declareConstructor(
            ktConstructor,
            ktClassOrObject.contextReceivers.mapNotNull { it.typeReference() },
            constructorDescriptor
        ) {
            when {
                constructorDescriptor.constructedClass.isEffectivelyExternal() ->
                    null

                ktConstructor.isConstructorDelegatingToSuper(context.bindingContext) ->
                    generateSecondaryConstructorBodyWithNestedInitializers(ktConstructor)

                else -> generateSecondaryConstructorBody(ktConstructor)
            }
        }
    }


    private inline fun declareConstructor(
        ktConstructorElement: KtPureElement,
        ktContextReceiversElements: List<KtPureElement>,
        constructorDescriptor: ClassConstructorDescriptor,
        generateBody: BodyGenerator.(IrConstructor) -> IrBody?
    ): IrConstructor {
        val startOffset = ktConstructorElement.getStartOffsetOfConstructorDeclarationKeywordOrNull() ?: ktConstructorElement.pureStartOffset
        val endOffset = ktConstructorElement.pureEndOffset
        val origin = IrDeclarationOrigin.DEFINED
        return context.symbolTable.descriptorExtension.declareConstructor(constructorDescriptor) {
            with(constructorDescriptor) {
                context.irFactory.createConstructor(
                    startOffset = startOffset,
                    endOffset = endOffset,
                    origin = origin,
                    name = context.symbolTable.nameProvider.nameForDeclaration(this),
                    visibility = visibility,
                    isInline = isInline,
                    isExpect = isExpect,
                    returnType = constructorDescriptor.returnType.toIrType(),
                    symbol = it,
                    isPrimary = isPrimary,
                    isExternal = isEffectivelyExternal(),
                )
            }.apply {
                metadata = DescriptorMetadataSource.Function(it.descriptor)
            }
        }.buildWithScope { irConstructor ->
            generateValueParameterDeclarations(irConstructor, ktConstructorElement, null, ktContextReceiversElements)
            if (context.configuration.generateBodies) {
                irConstructor.body = createBodyGenerator(irConstructor.symbol).generateBody(irConstructor)
            }
        }
    }

    fun generateSyntheticFunctionParameterDeclarations(irFunction: IrFunction) {
        val descriptor = irFunction.descriptor
        val typeParameters =
            if (descriptor is PropertyAccessorDescriptor)
                descriptor.correspondingProperty.typeParameters
            else
                descriptor.typeParameters
        declarationGenerator.generateScopedTypeParameterDeclarations(irFunction, typeParameters)
        generateValueParameterDeclarations(irFunction, null, null, emptyList(), withDefaultValues = false)
    }

    private fun generateValueParameterDeclarations(
        irFunction: IrFunction,
        ktParameterOwner: KtPureElement?,
        ktReceiverParameterElement: KtPureElement?,
        ktContextReceiverParameterElements: List<KtPureElement>,
        withDefaultValues: Boolean = true
    ) {
        val functionDescriptor = irFunction.descriptor

        functionDescriptor.dispatchReceiverParameter?.let {
            irFunction.parameters += generateReceiverParameterDeclaration(
                it, ktParameterOwner,
                irFunction, IrParameterKind.DispatchReceiver
            )
        }

        irFunction.parameters += functionDescriptor.contextReceiverParameters.mapIndexed { i, contextReceiver ->
            declareParameter(
                contextReceiver, ktContextReceiverParameterElements.getOrNull(i) ?: ktParameterOwner,
                irFunction, IrParameterKind.Context, null, i
            )
        }

        functionDescriptor.extensionReceiverParameter?.let {
            irFunction.parameters += generateReceiverParameterDeclaration(
                it, ktReceiverParameterElement ?: ktParameterOwner,
                irFunction, IrParameterKind.ExtensionReceiver
            )
        }

        val bodyGenerator = createBodyGenerator(irFunction.symbol)

        val contextReceiverParametersCount = functionDescriptor.contextReceiverParameters.size

        // Declare all the value parameters up first.
        irFunction.parameters += functionDescriptor.valueParameters.mapIndexed { i, valueParameterDescriptor ->
            val ktParameter = DescriptorToSourceUtils.getSourceFromDescriptor(valueParameterDescriptor) as? KtParameter
            declareParameter(
                valueParameterDescriptor, ktParameter,
                irFunction, IrParameterKind.Regular, null, i + contextReceiverParametersCount
            )
        }
        // Only after value parameters have been declared, generate default values. This ensures
        // that forward references to other parameters works in default value lambdas. For example:
        //
        // fun f(f1: () -> String = { f2() },
        //       f2: () -> String) = f1()
        if (withDefaultValues) {
            irFunction.parameters.filter { it.kind == IrParameterKind.Regular }.forEachIndexed { index, irValueParameter ->
                val valueParameterDescriptor = functionDescriptor.valueParameters[index]
                val ktParameter = DescriptorToSourceUtils.getSourceFromDescriptor(valueParameterDescriptor) as? KtParameter
                irValueParameter.defaultValue = ktParameter?.defaultValue?.let { defaultValue ->
                    val inAnnotation =
                        (valueParameterDescriptor.containingDeclaration as? ConstructorDescriptor)?.isAnnotationConstructor() == true
                    if (inAnnotation) {
                        generateDefaultAnnotationParameterValue(defaultValue, valueParameterDescriptor)
                    } else if (context.configuration.skipBodies) {
                        context.irFactory.createExpressionBody(
                            IrErrorExpressionImpl(
                                defaultValue.startOffsetSkippingComments,
                                defaultValue.endOffset,
                                context.irBuiltIns.nothingType,
                                SKIP_BODIES_ERROR_DESCRIPTION,
                            )
                        )
                    } else {
                        bodyGenerator.generateExpressionBody(defaultValue)
                    }
                }
            }
        }
    }

    private fun generateReceiverParameterDeclaration(
        receiverParameterDescriptor: ReceiverParameterDescriptor,
        ktElement: KtPureElement?,
        irOwnerElement: IrElement,
        kind: IrParameterKind,
    ): IrValueParameter {
        val name = if (context.languageVersionSettings.supportsFeature(LanguageFeature.NewCapturedReceiverFieldNamingConvention)) {
            if (ktElement is KtFunctionLiteral) {
                val label = getCallLabelForLambdaArgument(ktElement, this.context.bindingContext)?.let {
                    it.takeIf(Name::isValidIdentifier) ?: "\$receiver"
                }
                // TODO: this can produce `$this$null` - expected?
                Name.identifier("\$this\$$label")
            } else null
        } else null
        return declareParameter(receiverParameterDescriptor, ktElement, irOwnerElement, kind, name)
    }

    private fun getCallLabelForLambdaArgument(declaration: KtFunctionLiteral, bindingContext: BindingContext): String? {
        val lambdaExpression = declaration.parent as? KtLambdaExpression ?: return null
        val lambdaExpressionParent = lambdaExpression.parent

        if (lambdaExpressionParent is KtLabeledExpression) {
            lambdaExpressionParent.name?.let { return it }
        }

        val callExpression = when (val argument = lambdaExpression.parent) {
            is KtLambdaArgument -> {
                argument.parent as? KtCallExpression ?: return null
            }
            is KtValueArgument -> {
                val valueArgumentList = argument.parent as? KtValueArgumentList ?: return null
                valueArgumentList.parent as? KtCallExpression ?: return null
            }
            else -> return null
        }

        val call = callExpression.getResolvedCall(bindingContext) ?: return null
        return call.resultingDescriptor.name.asString()
    }

    private fun declareParameter(
        descriptor: ParameterDescriptor,
        ktElement: KtPureElement?,
        irOwnerElement: IrElement,
        kind: IrParameterKind,
        name: Name? = null,
        index: Int? = null
    ): IrValueParameter {
        var origin: IrDeclarationOrigin = IrDeclarationOrigin.DEFINED
        if (ktElement is KtParameter) {
            if (ktElement.isSingleUnderscore) {
                origin = IrDeclarationOrigin.UNDERSCORE_PARAMETER
            } else if (ktElement.destructuringDeclaration != null) {
                origin = IrDeclarationOrigin.DESTRUCTURED_OBJECT_PARAMETER
            }
        }
        return context.symbolTable.descriptorExtension.declareValueParameter(
            ktElement?.pureStartOffset ?: irOwnerElement.startOffset,
            ktElement?.pureEndOffset ?: irOwnerElement.endOffset,
            origin,
            descriptor, kind, descriptor.type.toIrType(),
            (descriptor as? ValueParameterDescriptor)?.varargElementType?.toIrType(),
            name,
            isAssignable = (irOwnerElement as? IrSimpleFunction)?.isTailrec == true && context.extensions.parametersAreAssignable
        )
    }

    private fun generateDefaultAnnotationParameterValue(
        valueExpression: KtExpression,
        valueParameterDescriptor: ValueParameterDescriptor
    ): IrExpressionBody {
        val constantDefaultValue =
            ConstantExpressionEvaluator.getConstant(valueExpression, context.bindingContext)?.toConstantValue(valueParameterDescriptor.type)
                ?: error("Constant value expected for default parameter value in annotation, got $valueExpression")
        val converted = context.constantValueGenerator.generateAnnotationValueAsExpression(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, constantDefaultValue, valueParameterDescriptor
        ) ?: error("Could not convert annotation default ${valueExpression.getElementTextWithContext()}")
        return context.irFactory.createExpressionBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET, converted)
    }
}
