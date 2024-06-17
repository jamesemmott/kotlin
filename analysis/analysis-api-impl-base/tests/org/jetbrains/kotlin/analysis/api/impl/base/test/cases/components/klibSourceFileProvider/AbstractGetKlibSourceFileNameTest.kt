/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.klibSourceFileProvider

import org.jetbrains.kotlin.analysis.api.KaAnalysisNonPublicApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.projectStructure.KaLibraryModule
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiBasedTest
import org.jetbrains.kotlin.analysis.test.framework.project.structure.ktTestModuleStructure
import org.jetbrains.kotlin.library.ToolingSingleFileKlibResolveStrategy
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf
import org.jetbrains.kotlin.library.metadata.parseModuleHeader
import org.jetbrains.kotlin.library.metadata.parsePackageFragment
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.metadata.deserialization.getExtensionOrNull
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.kotlin.util.DummyLogger
import kotlin.test.fail
import org.jetbrains.kotlin.konan.file.File as KonanFile

/**
 * Reads through the declarations provided in the .klib and renders their `klibSourceFile`
 */
abstract class AbstractGetKlibSourceFileNameTest : AbstractAnalysisApiBasedTest() {
    @OptIn(KaAnalysisNonPublicApi::class)
    override fun doTest(testServices: TestServices) {
        val mainModule = testServices.ktTestModuleStructure.mainModules
            .let { modules -> if (modules.size == 1) modules.first() else fail("Expected single main module. Found $modules") }

        val libraryModule = mainModule.ktModule as? KaLibraryModule
            ?: fail("Expected main module '${mainModule.ktModule}' to be '${KaLibraryModule::class.simpleName}'")

        val actual = StringBuilder()
        actual.appendLine("klib declarations:")

        analyze(libraryModule) {
            val binaryRoot = libraryModule.binaryRoots.singleOrNull() ?: fail("Expected single binary root")
            val library = ToolingSingleFileKlibResolveStrategy.tryResolve(KonanFile(binaryRoot), DummyLogger) ?: fail("Failed loading klib")
            val headerProto = parseModuleHeader(library.moduleHeaderData)

            val packageMetadataSequence = headerProto.packageFragmentNameList.asSequence().flatMap { packageFragmentName ->
                library.packageMetadataParts(packageFragmentName).asSequence().map { packageMetadataPart ->
                    library.packageMetadata(packageFragmentName, packageMetadataPart)
                }
            }

            packageMetadataSequence.forEach { packageMetadata ->
                val packageFragmentProto = parsePackageFragment(packageMetadata)
                val nameResolver = NameResolverImpl(packageFragmentProto.strings, packageFragmentProto.qualifiedNames)
                val packageFqName = packageFragmentProto.`package`.getExtensionOrNull(KlibMetadataProtoBuf.packageFqName)
                    ?.let { packageFqNameStringIndex -> nameResolver.getPackageFqName(packageFqNameStringIndex) }
                    ?.let { fqNameString -> FqName(fqNameString) }
                    ?: fail("Missing packageFqName")


                packageFragmentProto.class_List.forEach { classProto ->
                    val classId = ClassId.fromString(nameResolver.getQualifiedClassName(classProto.fqName))
                    val classSymbol = findClass(classId) ?: fail("Failed to find symbol '$classId'")
                    actual.appendLine("Classifier: ${classSymbol.classId}; klibSourceFile: ${classSymbol.klibSourceFileName}")
                }

                val propertyNames = packageFragmentProto.`package`.propertyList
                    .map { propertyProto -> nameResolver.getName(propertyProto.name) }

                val functionNames = packageFragmentProto.`package`.functionList
                    .map { functionProto -> nameResolver.getName(functionProto.name) }

                val callableNames = (propertyNames + functionNames).distinct()
                callableNames.forEach { callableName ->
                    findTopLevelCallables(packageFqName, callableName).forEach { symbol ->
                        actual.appendLine("Callable: ${symbol.callableId}; klibSourceFile: ${symbol.klibSourceFileName}")
                    }
                }
            }
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(actual.toString())
    }
}