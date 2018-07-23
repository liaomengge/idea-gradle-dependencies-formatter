package com.github.platan.idea.dependencies.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase

abstract class IntentionTestBase extends LightCodeInsightFixtureTestCase {

    protected final String intention

    IntentionTestBase(String intentionName) {
        assert intentionName != null
        this.intention = intentionName
    }

    protected void doTextTest(String given, String expected) {
        myFixture.configureByText("build.groovy", given)
        List<IntentionAction> list = myFixture.filterAvailableIntentions(intention)
        assert list.size() == 1, "An intention '$intention' should be applicable to: \n$given\n"
        myFixture.launchAction(list.first())
        PostprocessReformattingAspect.getInstance(project).doPostponedFormatting()
        myFixture.checkResult(expected)
    }

    protected void doAntiTest(String given) {
        myFixture.configureByText("build.groovy", given)
        assert !myFixture.filterAvailableIntentions(intention), "An intention '$intention' should not be applicable to: \n$given\n"
    }

    protected void doTest() {
        def baseName = getTestName(false).replaceFirst('_', '')
        def extension = ".groovy"
        def file = baseName + extension
        myFixture.configureByFile(file)
        List<IntentionAction> list = myFixture.filterAvailableIntentions(intention)
        assert list.size() == 1, "An intention '$intention' should be applicable to test case from file: $file\n"
        myFixture.launchAction(list.first())
        PostprocessReformattingAspect.getInstance(project).doPostponedFormatting()
        myFixture.checkResultByFile(baseName + "_after" + extension)
    }

}
