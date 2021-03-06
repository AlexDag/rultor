/**
 * Copyright (c) 2009-2015, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.web;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.jcabi.xml.XML;
import com.rexsl.page.BasePage;
import com.rexsl.page.BaseResource;
import com.rexsl.page.Inset;
import com.rexsl.page.Resource;
import com.rexsl.page.auth.AuthInset;
import com.rexsl.page.auth.Github;
import com.rexsl.page.auth.Identity;
import com.rexsl.page.auth.Provider;
import com.rexsl.page.inset.FlashInset;
import com.rexsl.page.inset.LinksInset;
import com.rexsl.page.inset.VersionInset;
import com.rultor.profiles.Profiles;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Abstract RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@Resource.Forwarded
@Inset.Default(LinksInset.class)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports" })
public class BaseRs extends BaseResource {

    /**
     * Test user.
     */
    public static final URN TEST_URN = URN.create("urn:facebook:1");

    /**
     * Test authentication provider.
     */
    private static final Provider TEST_PROVIDER = new Provider() {
        @Override
        public Identity identity() {
            final Identity identity;
            if (Manifests.read("Rultor-DynamoKey").startsWith("AAAAA")) {
                identity = new Identity.Simple(
                    BaseRs.TEST_URN,
                    "localhost",
                    URI.create("http://doc.rultor.com/images/none.png")
                );
            } else {
                identity = Identity.ANONYMOUS;
            }
            return identity;
        }
    };

    /**
     * Flash.
     * @return The inset with flash
     */
    @Inset.Runtime
    @NotNull(message = "flash can never be NULL")
    public final FlashInset flash() {
        return new FlashInset(this);
    }

    /**
     * Inset with a version of the product.
     * @return The inset
     */
    @Inset.Runtime
    @NotNull(message = "version can never be NULL")
    public final Inset insetVersion() {
        return new VersionInset(
            Manifests.read("Rultor-Version"),
            // @checkstyle MultipleStringLiterals (1 line)
            Manifests.read("Rultor-Revision"),
            Manifests.read("Rultor-Date")
        );
    }

    /**
     * Supplementary inset.
     * @return The inset
     * @throws IOException If fails
     */
    @Inset.Runtime
    @NotNull(message = "supplementary inset can never be NULL")
    public final Inset insetSupplementary() throws IOException {
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                builder.type(MediaType.TEXT_XML);
                builder.header(HttpHeaders.VARY, "Cookie");
                builder.header(
                    "X-Rultor-Revision",
                    Manifests.read("Rultor-Revision")
                );
            }
        };
    }

    /**
     * Toggles inset.
     * @return The inset
     * @throws IOException If fails
     */
    @Inset.Runtime
    @NotNull(message = "toggles inset can never be NULL")
    public final Inset toggles() throws IOException {
        return new TogglesInset(this);
    }

    /**
     * Authentication inset.
     * @return The inset
     */
    @Inset.Runtime
    @NotNull(message = "auth inset can never be NULL")
    public final AuthInset auth() {
        // @checkstyle LineLength (4 lines)
        return new AuthInset(this, Manifests.read("Rultor-SecurityKey"))
            .with(new Github(this, Manifests.read("Rultor-GithubId"), Manifests.read("Rultor-GithubSecret")))
            .with(BaseRs.TEST_PROVIDER);
    }

    /**
     * If admin permissions required.
     */
    protected final void adminOnly() {
        final String self = this.auth().identity().urn().toString();
        if (!"urn:github:526301".equals(self)) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "sorry, but this entrance is \"staff only\"",
                Level.WARNING
            );
        }
    }

    /**
     * Get all talks.
     * @return The talks
     */
    @NotNull(message = "Talks can't be NULL")
    protected final Talks talks() {
        return Talks.class.cast(
            this.servletContext().getAttribute(Talks.class.getName())
        );
    }

    /**
     * Can I see this talk?
     * @param talk Talk to use
     * @return TRUE if access granted
     * @throws IOException If fails
     */
    protected final boolean granted(final Talk talk) throws IOException {
        final XML xml;
        try {
            xml = new Profiles().fetch(talk).read();
        } catch (final Profile.ConfigException ex) {
            throw this.flash().redirect(this.uriInfo().getBaseUri(), ex);
        }
        final boolean granted;
        final Collection<String> readers = xml.xpath(
            "/p/entry[@key='readers']/item/text()"
        );
        if (readers.isEmpty()) {
            granted = true;
        } else {
            final String self = this.auth().identity().urn().toString();
            granted = Iterables.any(
                readers,
                new Predicate<String>() {
                    @Override
                    public boolean apply(final String input) {
                        return input.trim().equals(self);
                    }
                }
            );
        }
        return granted;
    }

}
