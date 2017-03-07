package com.github.platan.idea.dependencies.intentions

import com.github.platan.idea.dependencies.gradle.Coordinate
import com.github.platan.idea.dependencies.gradle.Coordinate.isStringNotationCoordinate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import org.jetbrains.plugins.groovy.intentions.base.Intention
import org.jetbrains.plugins.groovy.intentions.base.PsiElementPredicate
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrString
import org.jetbrains.plugins.groovy.lang.psi.util.ErrorUtil.containsError
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.DOUBLE_QUOTES
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.TRIPLE_DOUBLE_QUOTES
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.escapeAndUnescapeSymbols
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.getStartQuote
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.isStringLiteral
import org.jetbrains.plugins.groovy.lang.psi.util.GrStringUtil.removeQuotes

class StringNotationToMapNotationIntention : Intention() {

    @Throws(IncorrectOperationException::class)
    override fun processIntention(element: PsiElement, project: Project, editor: Editor) {
        val elementWithStringNotation = findElement(element)
        val quote = getStartQuote(elementWithStringNotation!!.text)
        val stringNotation = removeQuotes(elementWithStringNotation.text)
        val mapNotation = Coordinate.parse(stringNotation).toMapNotation(quote)
        val argumentList = GroovyPsiElementFactory.getInstance(project).createArgumentListFromText(mapNotation)
        if (isInterpolableString(quote)) {
            replaceGStringMapValuesToString(argumentList, project)
        }
        if (elementWithStringNotation.parent is GrCommandArgumentList && element.text == element.parent.text) {
            elementWithStringNotation.parent.replace(argumentList)
        } else {
            elementWithStringNotation.replace(argumentList)
        }
    }

    private fun isInterpolableString(quote: String): Boolean {
        return quote == DOUBLE_QUOTES || quote == TRIPLE_DOUBLE_QUOTES
    }

    private fun replaceGStringMapValuesToString(map: GrArgumentList, project: Project) {
        for (psiElement in map.children) {
            val lastChild = psiElement.lastChild
            if (lastChild is GrLiteral && lastChild !is GrString) {
                val stringWithoutQuotes = removeQuotes(lastChild.text)
                val unescaped = escapeAndUnescapeSymbols(stringWithoutQuotes, "", "\"$", StringBuilder())
                val string = "'$unescaped'"
                lastChild.replace(GroovyPsiElementFactory.getInstance(project).createExpressionFromText(string))
            }
        }
    }

    override fun getElementPredicate(): PsiElementPredicate {
        return PsiElementPredicate { element ->
            findElement(element) != null
        }
    }

    private fun findElement(element: PsiElement): PsiElement? {
        return listOf(element, element.parent.parent.lastChild.firstChild).find { element ->
            element?.parent is GrArgumentList
                    && element is GrLiteral
                    && !containsError(element)
                    && isStringLiteral(element)
                    && isStringNotationCoordinate(removeQuotes(element.text))
        }
    }

    override fun getText(): String {
        return "Convert to map notation"
    }

    override fun getFamilyName(): String {
        return "Convert string notation to map notation"
    }
}
