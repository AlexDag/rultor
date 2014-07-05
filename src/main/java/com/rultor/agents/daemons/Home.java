/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.rultor.spi.Repo;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import org.apache.commons.lang3.CharEncoding;

/**
 * Home page of a daemon.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class Home {

    /**
     * Repo.
     */
    private final transient Repo repo;

    /**
     * Talk.
     */
    private final transient Talk talk;

    /**
     * Hash.
     */
    private final transient String hash;

    /**
     * Ctor.
     * @param rpo Repo
     * @param tlk Talk
     * @param hsh Hash
     */
    public Home(final Repo rpo, final Talk tlk, final String hsh) {
        this.repo = rpo;
        this.talk = tlk;
        this.hash = hsh;
    }

    /**
     * Get its URI.
     * @return URI
     * @throws IOException If fails
     */
    public URI uri() throws IOException {
        return URI.create(
            String.format(
                "http://www.rultor.com/d/%d/%s/%s",
                this.repo.number(),
                URLEncoder.encode(this.talk.name(), CharEncoding.UTF_8),
                this.hash
            )
        );
    }

}
