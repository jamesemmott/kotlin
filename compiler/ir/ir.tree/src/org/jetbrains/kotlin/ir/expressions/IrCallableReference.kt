/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.symbols.*

interface IrCallableReference : IrMemberAccessExpression, IrDeclarationReference, IrAttributeContainer {
    override val descriptor: CallableDescriptor
}

interface IrFunctionReference : IrCallableReference {
    override val descriptor: FunctionDescriptor
    override val symbol: IrFunctionSymbol
}

interface IrPropertyReference : IrCallableReference {
    override val descriptor: PropertyDescriptor
    override val symbol: IrPropertySymbol
    val field: IrFieldSymbol?
    val getter: IrSimpleFunctionSymbol?
    val setter: IrSimpleFunctionSymbol?
}

interface IrLocalDelegatedPropertyReference : IrCallableReference {
    override val descriptor: VariableDescriptorWithAccessors
    override val symbol: IrLocalDelegatedPropertySymbol
    val delegate: IrVariableSymbol
    val getter: IrSimpleFunctionSymbol
    val setter: IrSimpleFunctionSymbol?
}
