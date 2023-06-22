package org.example

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.symbol.Variance.CONTRAVARIANT
import com.google.devtools.ksp.symbol.Variance.COVARIANT
import com.google.devtools.ksp.symbol.Variance.INVARIANT
import com.google.devtools.ksp.symbol.Variance.STAR
import com.google.devtools.ksp.validate
import java.io.OutputStream

// Gesamte Verarbeitungslogik
class FunctionProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    // Durch die Verwendung des operator Schlüsselworts können spezifische Implementierung für einen Operator
    // basierend auf den beteiligten Operanden bereitstellen.
    // ohne das Überladen des += operators würde unten die += aufruf nicht funktionieren.
    //+= Operator für die OutputStreamKlasse zu überladen.
    operator fun OutputStream.plusAssign(str: String) {
        this.write(str.toByteArray())
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols =
            resolver // resolver ist für die Auflösung von Symbolen während des Kompilierungsprozesses verantwortlich.
                .getSymbolsWithAnnotation("org.example.Function")
                .filterIsInstance<KSClassDeclaration>()

        // Verlassen des Prozessors, wenn nichts mit @Function annotiert ist.
        if (!symbols.iterator()
                .hasNext()
        ) return emptyList() // Da symbols keine Liste ist, müssen wir schauen, ob der Zeiger gleich null ist

        // Die generierte Datei befindet sich unter:
        // build/generated/ksp/main/kotlin/org/example/GeneratedFunctions.kt
        // in dieser Datei befindet sich alle generierten Funktionen, denke ich!?
        val file: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = "org.example",
            fileName = "GeneratedFunctions"
        )

        // Das macht einfach das oben im file package org.example steht.
        file += "package org.example\n"

        // Verarbeitung jeder Klassendeklaration, die mit @Function annotiert ist.
        symbols.forEach {
            //TODO: Zeile:79 Hier werden die Regeln für das generieren festgelegt.
            it.accept(Visitor(file), Unit)
        }

        // Don't forget to close the out stream.
        file.close()

        return symbols.filterNot {
            it.validate()
        }.toList()
    }

    //Die gesamte Codegenerierungs-Logik unseres Prozessors wird innerhalb Visitorder Klasse implementiert.
    inner class Visitor(private val file: OutputStream) : KSVisitorVoid() {

        // In KSP wird die Schnittstellendeklaration durch einen KSClassDeclaration Type dargestellt.
        // KSClassDeclaration kann jeder typ sein, wie Data class interface, class usw.
        // Es muss sichergestellt werden, dass die annotierte Klasse tatsächlich ein Interface ist.
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            //indem wir die Eigenschaft überprüfen. Andernfalls stellen wir die Verarbeitung abgebrochen. data class interface classKind.
            if (classDeclaration.classKind != ClassKind.OBJECT) {
                logger.error(
                    "Only interface can be annotated with @Function und das ist hier kein interface!!!!",
                    classDeclaration
                )
                return
            }

            // Getting the @Function annotation object.
            // Da interfaces mit vielen Annotationen bestückt seinen können, müssen anhand des Namens Annotationen gefunden werden.
            //TODO: wieso gebe ich das hier zurück? Weil ich das in Zeile 103 brauche.
            val annotation: KSAnnotation = classDeclaration.annotations.first {
                it.shortName.asString() == "Function"
            }

            // Hier werden die Argumente der Annotations-Klasse ausgewertet.
            //TODO: das nameArgument wird in Zeile 110 verwendet.
            val nameArgument: KSValueArgument = annotation.arguments
                .first {
                    it.name?.asString() == "erstesArgument"
                }

            // Hier wird das Argument ausgewertet/ verwendet
            //TODO: functionName wird in 122 verwendet.
            val functionName = nameArgument.value as String

            // Abrufen der Liste der properties in der kommentierten Schnittstelle.
            //TODO: verwenden von Properties in 121
            val properties: Sequence<KSPropertyDeclaration> = classDeclaration.getAllProperties()
                .filter {
                    it.validate()
                }

            // Funktionssignatur generieren / Funktionskopf
            file += "\n"
            if (properties.iterator().hasNext()) {
                file += "fun $functionName(\n"

                // Iteration durch jede Eigenschaft, um sie in Funktionsargumente zu übersetzen.
                //TODO: springt zur anderen Funktion - die für die Übergabeparameter zuständig ist.
                properties.forEach {
                    visitPropertyDeclaration(it, Unit)
                }
                file += ") {\n"

            } else {
                // Andernfalls wird eine Funktion ohne Argumente erzeugt.
                file += "fun $functionName() {\n"
            }

            // Erzeugen von Funktionskörpern.
            file += "    println(\"Hallo aus Funktion -> $functionName\")\n"
            file += "}\n"

        }

        // KSClassDeclaration kann jeder typ sein, wie Data class interface, class usw. Wieso muss nicht geprüft werden,
        // ob es ein Interface ist?
        // Es muss sichergestellt werden, dass die annotierte Klasse tatsächlich ein Interface ist.
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            // Erzeugen des Argumentnamens.
            val argumentName = property.simpleName.asString()
            file += "    $argumentName: "

            // Erzeugen des Argumenttyps.
            val resolvedType: KSType = property.type.resolve()
            // Benötigt viel performance.
            file += resolvedType.declaration.qualifiedName?.asString() ?: run {
                logger.error("Invalid property type", property)
                return
            }

            // Generierung von generischen Parametern, falls vorhanden.
            val genericArguments: List<KSTypeArgument> = property.type.element?.typeArguments ?: emptyList()
            //TODO: aufruf der weiteren Interface Methode
            visitTypeArguments(genericArguments)

            // Handhabung im Fall Null.
            file += if (resolvedType.nullability == Nullability.NULLABLE) "?" else ""

            file += ",\n"
        }

        // Diese Funktion übernimmt die Liste der Typ-argumente.
        // Es werden die Spitzen Klammern der Generics gesetzt.
        private fun visitTypeArguments(typeArguments: List<KSTypeArgument>) {
            if (typeArguments.isNotEmpty()) {
                file += "<"
                typeArguments.forEachIndexed { i, arg ->
                    //TODO: hier wird die nächste Funktion aufgerufen
                    visitTypeArgument(arg, data = Unit)
                    if (i < typeArguments.lastIndex) file += ", "
                }
                file += ">"
            }
        }

        override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) {
            // Die Unterschiedlichen Generic Typen werden hier analysiert.
            when (val variance: Variance = typeArgument.variance) {
                STAR -> {
                    file += "*"
                    return
                }
                COVARIANT, CONTRAVARIANT -> {
                    file += variance.label // 'out' or 'in'
                    file += " "
                }
                INVARIANT -> {
                    // do nothing
                }
            }

            // Nun das Ergründen der tatsächlichen Generic Typen.
            val resolvedType: KSType? = typeArgument.type?.resolve()
            file += resolvedType?.declaration?.qualifiedName?.asString() ?: run {
                logger.error("Invalid type argument", typeArgument)
                return
            }

            // Generating nested generic parameters if any
            val genericArguments: List<KSTypeArgument> = typeArgument.type?.element?.typeArguments ?: emptyList()
            visitTypeArguments(genericArguments)

            // Type<OtherType<OtherType>>

            // Handling nullability
            file += if (resolvedType?.nullability == Nullability.NULLABLE) "?" else ""
        }
    }
}