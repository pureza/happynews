package io.github.pureza.happynews.validation;

import io.github.pureza.happynews.newsgroup.ArticleHeader;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ArticleValidatorTest {

    @Test
    public void isValidArticleIdAcceptsValidId() {
        assertThat(new ArticleValidator().isValidArticleId("<1@host>"), is(true));
        assertThat(new ArticleValidator().isValidArticleId("<1@host.pt>"), is(true));
    }


    @Test
    public void isValidArticleIdDoesNotAcceptInvalidIds() {
        assertThat(new ArticleValidator().isValidArticleId("1@host"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@host"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<blah@host>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@hos<t>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@hos>t>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<@host>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId(" <1@host>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@host> "), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@ho st>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@ho@st>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@.host>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@host.>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@a..b>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@.>"), is(false));
        assertThat(new ArticleValidator().isValidArticleId("<1@..>"), is(false));
    }


    @Test
    public void isValidFromAcceptsValidFromField() {
        assertThat(new ArticleValidator().isValidFrom("<user@host>"), is(true));
        assertThat(new ArticleValidator().isValidFrom("<_@_._>"), is(true));
        assertThat(new ArticleValidator().isValidFrom("<a.b@host.com>"), is(true));
        assertThat(new ArticleValidator().isValidFrom("My name <a.b@host.com>"), is(true));
    }


    @Test
    public void isValidFromDoesNotAcceptInvalidFroms() {
        assertThat(new ArticleValidator().isValidFrom("1@host"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<1@host"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<1>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<1@hos<t>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<1@hos>t>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<1@>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<@host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom(" <1@host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<1@host> "), is(false));
        assertThat(new ArticleValidator().isValidFrom("<1@ho st>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<1@ho@st>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<.@host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<..@host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<.abc@host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<abc.@host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<ab..c@host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<abc@.host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<abc@host.>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<abc@a..b>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<abc@.>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<abc@..>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<123abc@host>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("<abc@123.com>"), is(false));
        assertThat(new ArticleValidator().isValidFrom("My name <abc@123.com>"), is(false));
    }


    @Test
    public void isValidHeaderAcceptsValidHeader() {
        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");

        assertThat(new ArticleValidator().isValidHeader(header), is(true));
    }


    @Test
    public void isValidHeaderFailsOnMissingNewsgroupsField() {
        ArticleHeader header = new ArticleHeader("");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");

        assertThat(new ArticleValidator().isValidHeader(header), is(false));
    }


    @Test
    public void isValidHeaderFailsOnEmptyNewsgroupsField() {
        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");

        assertThat(new ArticleValidator().isValidHeader(header), is(false));
    }


    @Test
    public void isValidHeaderFailsOnInvalidNewsgroupsField() {
        ArticleHeader header = new ArticleHeader("");
        header.put("Newsgroups", "not a valid, name,, ok");
        header.put("From", "<user@host.org>");
        header.put("Subject", "Hello, world");

        assertThat(new ArticleValidator().isValidHeader(header), is(false));
    }


    @Test
    public void isValidHeaderFailsOnMissingSubjectField() {
        ArticleHeader header = new ArticleHeader("");
        header.put("From", "<user@host.org>");
        header.put("Newsgroups", "happynews.users");

        assertThat(new ArticleValidator().isValidHeader(header), is(false));
    }


    @Test
    public void isValidHeaderFailsOnMissingFromField() {
        ArticleHeader header = new ArticleHeader("");
        header.put("Subject", "Hello, world");
        header.put("Newsgroups", "happynews.users");

        assertThat(new ArticleValidator().isValidHeader(header), is(false));
    }


    @Test
    public void isValidHeaderFailsOnInvalidFromField() {
        ArticleHeader header = new ArticleHeader("");
        header.put("Subject", "Hello, world");
        header.put("Newsgroups", "happynews.users");
        header.put("From", "<inva..lid@host.com>");

        assertThat(new ArticleValidator().isValidHeader(header), is(false));
    }
}
