package com.github.platan.idea.dependencies.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase

class MultipleIntentionTest extends LightCodeInsightFixtureTestCase {

    protected void given(String given) {
        myFixture.configureByText("build.gradle", given)
    }

    protected void when(String intention) {
        List<IntentionAction> list = myFixture.filterAvailableIntentions(intention)
        assert list.size() == 1, "An intention '$intention' should be applicable to: \n$myFixture.file.text\n"
        myFixture.launchAction(list.first())
    }

    protected void then(String expected) {
        PostprocessReformattingAspect.getInstance(project).doPostponedFormatting()
        myFixture.checkResult(expected)
    }

    void test_convert_to_string_notation_and_to_map_notation() {
        given('''dependencies {
    compile group: 'com.go<caret>ogle.guava', name: 'guava', version: '18.0'
}''')

        when('Convert to string notation')
        when('Convert to map notation')

        then('''dependencies {
    compile group: 'com.google.guava', name: 'guava', version: '18.0\'
}''')
    }

    void test_convert_to_string_notation_and_to_map_notation_with_caret_at_configuration_name() {
        given('''dependencies {
    com<caret>pile group: 'com.google.guava', name: 'guava', version: '18.0'
}''')

        when('Convert to string notation')
        when('Convert to map notation')

        then('''dependencies {
    compile group: 'com.google.guava', name: 'guava', version: '18.0\'
}''')
    }

    void test_convert_to_map_notation_and_to_string_notation() {
        given('''dependencies {
    compile 'com.go<caret>ogle.guava:guava:18.0'
}''')

        when('Convert to map notation')
        when('Convert to string notation')

        then('''dependencies {
    compile 'com.google.guava:guava:18.0'
}''')
    }

    void test_convert_to_map_notation_and_to_string_notation_with_caret_at_configuration_name() {
        given('''dependencies {
    com<caret>pile 'com.google.guava:guava:18.0'
}''')

        when('Convert to map notation')
        when('Convert to string notation')

        then('''dependencies {
    compile 'com.google.guava:guava:18.0'
}''')
    }

}
