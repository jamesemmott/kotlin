/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.test.framework.services

import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import org.jetbrains.kotlin.analysis.api.standalone.base.projectStructure.registerApplicationServices
import org.jetbrains.kotlin.analysis.api.standalone.base.projectStructure.registerProjectExtensionPoints
import org.jetbrains.kotlin.analysis.api.standalone.base.projectStructure.registerProjectServices
import org.jetbrains.kotlin.analysis.test.framework.projectStructure.ktTestModuleStructureProvider
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.registerProjectModelServices
import org.jetbrains.kotlin.test.services.PreAnalysisHandler
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.compilerConfigurationProvider

class ProjectStructureInitialisationPreAnalysisHandler(
    testServices: TestServices,
    private val configurator: AnalysisApiTestConfigurator,
) : PreAnalysisHandler(testServices) {

    override fun preprocessModuleStructure(moduleStructure: TestModuleStructure) {
        checkAllModulesHaveTheSameProject(moduleStructure)

        testServices.environmentManager.initializeEnvironment()

        val project = testServices.environmentManager.getProject() as MockProject
        val application = testServices.environmentManager.getApplication() as MockApplication

        configurator.serviceRegistrars.registerApplicationServices(application, testServices)
        createAndRegisterKtModules(moduleStructure, project)
        configurator.serviceRegistrars.registerProjectExtensionPoints(project, testServices)
        configurator.serviceRegistrars.registerProjectServices(project, testServices)
        testServices.environmentManager.initializeProjectStructure()
        configurator.serviceRegistrars.registerProjectModelServices(project, testServices)
    }

    private fun createAndRegisterKtModules(moduleStructure: TestModuleStructure, project: MockProject) {
        val ktTestModuleStructure = configurator.createModules(moduleStructure, testServices, project)
        testServices.ktTestModuleStructureProvider.registerModuleStructure(ktTestModuleStructure)
    }

    private fun checkAllModulesHaveTheSameProject(moduleStructure: TestModuleStructure) {
        val modules = moduleStructure.modules
        val project = testServices.compilerConfigurationProvider.getProject(moduleStructure.modules.first()) as MockProject
        check(modules.all { testServices.compilerConfigurationProvider.getProject(it) == project })
    }
}