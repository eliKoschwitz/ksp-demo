package org.example

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Alle diese Argumente stammen aus der SymbolProcessingEnvironmentInstanz,
 * die uns von KSP in FunctionProcessorProvider.create
 * der Funktion bereitgestellt wird.
 */
class FunctionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FunctionProcessor(
            codeGenerator = environment.codeGenerator, // Erstellung Dateien mit Prozessor generierten Code
            logger = environment.logger, // Einfach ein logger von KSP
            options = environment.options // Schlüsselwertoptionen | key-value options | options: Map<String, String>
        )
    }
}