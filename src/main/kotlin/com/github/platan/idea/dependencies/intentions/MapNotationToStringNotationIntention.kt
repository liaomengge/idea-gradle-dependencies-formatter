package com.github.platan.idea.dependencies.intentions

import com.github.platan.idea.dependencies.gradle.Coordinate
import com.github.platan.idea.dependencies.sort.DependencyUtil.isGstring
import com.github.platan.idea.dependencies.sort.DependencyUtil.isInterpolableString
import com.github.platan.idea.dependencies.sort.DependencyUtil.toMap
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import org.jetbrains.plugins.groovy.intentions.base.Intention
import org.jetbrains.plugins.groovy.intentions.base.PsiElementPredicate
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.arguments.GrArgumentListImpl
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.getStartQuote

class MapNotationToStringNotationIntention : Intention() {

    @Throws(IncorrectOperationException::class)
    override fun processIntention(element: PsiElement, project: Project, editor: Editor) {
        val argumentList = findElement(element)
        val namedArguments = argumentList!!.namedArguments
        val stringNotation = toStringNotation(namedArguments)
        for (namedArgument in namedArguments) {
            namedArgument.delete()
        }
        val expressionFromText = GroovyPsiElementFactory.getInstance(project).createExpressionFromText(stringNotation)
        argumentList.add(expressionFromText)
    }

    private fun toStringNotation(namedArguments: Array<GrNamedArgument>): String {
        val map = toMap(namedArguments)
        val coordinate = Coordinate.fromMap(map)
        val containsGstringValue = containsGstringValue(namedArguments)
        val quote = if (containsGstringValue) '"' else '\''
        return "$quote${coordinate.toStringNotation()}$quote"
    }

    private fun containsGstringValue(namedArguments: Array<GrNamedArgument>): Boolean {
        var containsGstringValue = false
        for (namedArgument in namedArguments) {
            val expression = namedArgument.expression
            val quote = getStartQuote(expression!!.text)
            if (isInterpolableString(quote) && isGstring(expression)) {
                containsGstringValue = true
                break
            }
        }
        return containsGstringValue
    }

    override fun getElementPredicate(): PsiElementPredicate {
        return PsiElementPredicate { element ->
            findElement(element) != null
        }
    }

    private fun findElement(element: PsiElement?): GrArgumentListImpl? {
        return listOf(element?.parent, element?.parent?.parent, element?.parent?.parent?.lastChild)
                .filterIsInstance<GrArgumentListImpl>()
                .find { !it.namedArguments.isEmpty() && Coordinate.isValidMap(toMap(it.namedArguments)) }
    }

    override fun getText(): String {
        return "Convert to string notation"
    }

    override fun getFamilyName(): String {
        return "Convert map notation to string notation"
    }
}
