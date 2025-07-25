/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.compiler.plugins.kotlin.k2

import com.intellij.lang.LighterASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtTryExpression

object ComposeErrors : KtDiagnosticsContainer() {
    // error goes on the composable call in a non-composable function
    val COMPOSABLE_INVOCATION by error0<PsiElement>()

    // error goes on the non-composable function with composable calls
    val COMPOSABLE_EXPECTED by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val NONREADONLY_CALL_IN_READONLY_COMPOSABLE by error0<PsiElement>()

    val CAPTURED_COMPOSABLE_INVOCATION by
    error2<PsiElement, FirVariableSymbol<*>, FirCallableSymbol<*>>()

    // composable calls are not allowed in try expressions
    // error goes on the `try` keyword
    val ILLEGAL_TRY_CATCH_AROUND_COMPOSABLE by error0<KtTryExpression>(
        ComposeSourceElementPositioningStrategies.TRY_KEYWORD
    )

    val MISSING_DISALLOW_COMPOSABLE_CALLS_ANNOTATION by error3<
            PsiElement,
            FirValueParameterSymbol, // unmarked
            FirValueParameterSymbol, // marked
            FirCallableSymbol<*>>()

    val DEPRECATED_OPEN_COMPOSABLE_DEFAULT_PARAMETER_VALUE by warning0<PsiElement>()

    val COMPOSABLE_SUSPEND_FUN by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val COMPOSABLE_FUN_MAIN by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val COMPOSABLE_PROPERTY_REFERENCE by error0<PsiElement>(
        ComposeSourceElementPositioningStrategies.DECLARATION_NAME_OR_DEFAULT
    )

    val COMPOSABLE_PROPERTY_BACKING_FIELD by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val COMPOSABLE_VAR by error0<PsiElement>(SourceElementPositioningStrategies.DECLARATION_NAME)

    val COMPOSE_INVALID_DELEGATE by error0<PsiElement>(
        ComposeSourceElementPositioningStrategies.DECLARATION_NAME_OR_DEFAULT
    )

    val MISMATCHED_COMPOSABLE_IN_EXPECT_ACTUAL by error0<PsiElement>(
        SourceElementPositioningStrategies.DECLARATION_NAME
    )

    val COMPOSE_APPLIER_CALL_MISMATCH by warning2<PsiElement, String, String>(
        SourceElementPositioningStrategy(
            LightTreePositioningStrategies.REFERENCED_NAME_BY_QUALIFIED,
            PositioningStrategies.CALL_EXPRESSION
        )
    )

    val COMPOSE_APPLIER_PARAMETER_MISMATCH by warning2<PsiElement, String, String>()

    val COMPOSE_APPLIER_DECLARATION_MISMATCH by warning0<PsiElement>(
        ComposeSourceElementPositioningStrategies.DECLARATION_NAME_OR_DEFAULT
    )

    val COMPOSABLE_INAPPLICABLE_TYPE by error1<PsiElement, ConeKotlinType>()

    val OPEN_COMPOSABLE_DEFAULT_PARAMETER_VALUE by error1<PsiElement, LanguageVersion>()

    val ABSTRACT_COMPOSABLE_DEFAULT_PARAMETER_VALUE by error1<PsiElement, LanguageVersion>()

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = ComposeErrorMessages
}

object ComposeSourceElementPositioningStrategies {
    private val PSI_TRY_KEYWORD: PositioningStrategy<KtTryExpression> =
        object : PositioningStrategy<KtTryExpression>() {
            override fun mark(element: KtTryExpression): List<TextRange> {
                element.tryKeyword?.let {
                    return markElement(it)
                }
                return PositioningStrategies.DEFAULT.mark(element)
            }
        }

    private val LIGHT_TREE_TRY_KEYWORD: LightTreePositioningStrategy =
        object : LightTreePositioningStrategy() {
            override fun mark(
                node: LighterASTNode,
                startOffset: Int,
                endOffset: Int,
                tree: FlyweightCapableTreeStructure<LighterASTNode>,
            ): List<TextRange> {
                val target = tree.findChildByType(node, KtTokens.TRY_KEYWORD) ?: node
                return markElement(target, startOffset, endOffset, tree, node)
            }
        }

    private val PSI_DECLARATION_NAME_OR_DEFAULT: PositioningStrategy<PsiElement> =
        object : PositioningStrategy<PsiElement>() {
            override fun mark(element: PsiElement): List<TextRange> {
                if (element is KtNamedDeclaration) {
                    return PositioningStrategies.DECLARATION_NAME.mark(element)
                }
                return PositioningStrategies.DEFAULT.mark(element)
            }
        }

    val TRY_KEYWORD = SourceElementPositioningStrategy(
        LIGHT_TREE_TRY_KEYWORD,
        PSI_TRY_KEYWORD
    )

    val DECLARATION_NAME_OR_DEFAULT = SourceElementPositioningStrategy(
        LightTreePositioningStrategies.DECLARATION_NAME,
        PSI_DECLARATION_NAME_OR_DEFAULT
    )
}
