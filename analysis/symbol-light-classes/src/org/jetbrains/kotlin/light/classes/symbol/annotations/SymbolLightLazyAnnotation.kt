/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.annotations

import com.intellij.psi.PsiAnnotationParameterList
import com.intellij.psi.PsiModifierList
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationApplication
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationOverview
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallElement

internal class SymbolLightLazyAnnotation private constructor(
    private val classId: ClassId,
    val annotationsProvider: AnnotationsProvider,
    private val annotationOverview: KtAnnotationOverview?,
    private val specialAnnotationApplication: KtAnnotationApplication?,
    owner: PsiModifierList,
) : SymbolLightAbstractAnnotation(owner) {
    init {
        require(annotationOverview != null || specialAnnotationApplication != null)
    }

    private val fqName: FqName = classId.asSingleFqName()

    val annotationApplication: Lazy<KtAnnotationApplication> = specialAnnotationApplication?.let(::lazyOf) ?: lazyPub {
        val applications = annotationsProvider[classId]
        applications.find { it.index == annotationOverview?.index }
            ?: error("expected index: ${annotationOverview?.index}, actual indices: ${applications.map { it.index }}")
    }

    constructor(
        classId: ClassId,
        annotationsProvider: AnnotationsProvider,
        annotationOverview: KtAnnotationOverview,
        owner: PsiModifierList,
    ) : this(
        classId = classId,
        annotationsProvider = annotationsProvider,
        annotationOverview = annotationOverview,
        specialAnnotationApplication = null,
        owner = owner,
    )

    constructor(
        classId: ClassId,
        annotationsProvider: AnnotationsProvider,
        annotationApplication: KtAnnotationApplication,
        owner: PsiModifierList,
    ) : this(
        classId = classId,
        annotationsProvider = annotationsProvider,
        annotationOverview = null,
        specialAnnotationApplication = annotationApplication,
        owner = owner,
    )

    override val kotlinOrigin: KtCallElement? get() = annotationApplication.value.psi

    override fun getQualifiedName(): String = fqName.asString()

    private val _parameterList: PsiAnnotationParameterList by lazyPub {
        if (annotationOverview?.hasArguments == true || !specialAnnotationApplication?.arguments.isNullOrEmpty()) {
            SymbolLightLazyAnnotationParameterList(this, lazyPub { annotationApplication.value.arguments })
        } else {
            SymbolLightEmptyAnnotationParameterList(this)
        }
    }

    override fun getParameterList(): PsiAnnotationParameterList = _parameterList

    override fun equals(other: Any?): Boolean = this === other ||
            other is SymbolLightLazyAnnotation &&
            other.fqName == fqName &&
            other.annotationOverview == annotationOverview &&
            other.specialAnnotationApplication == specialAnnotationApplication &&
            annotationsProvider.isTheSameAs(other.annotationsProvider) &&
            other.parent == parent

    override fun hashCode(): Int = fqName.hashCode()
}
