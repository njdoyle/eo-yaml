/**
 * Copyright (c) 2016-2020, Mihai Emil Andronache
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.amihaiemil.eoyaml;

import java.util.*;

/**
 * YAML sequence implementation (rt means runtime).
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * @see http://yaml.org/spec/1.2/spec.html#sequence//
 */
final class RtYamlSequence extends BaseYamlSequence {

    /**
     * Comments referring to this sequence.
     */
    private Comment comment;

    /**
     * Nodes in this sequence.
     */
    private final List<YamlNode> nodes = new LinkedList<>();

    /**
     * Ctor.
     * @param elements Elements of this sequence.
     */
    RtYamlSequence(final Collection<YamlNode> elements) {
        this(elements, new ArrayList<>(), "");
    }

    /**
     * Constructor.
     * @param elements Elements of this sequence.
     * @param comments Comments referring to the elements of this sequence.
     * @param comment Comment referring to this sequence itself.
     */
    RtYamlSequence(
        final Collection<YamlNode> elements,
        final List<Comment> comments,
        final String comment
    ) {
        super(new RtComments(comments));
        this.nodes.addAll(elements);
        this.comment = new BuiltComment(this, comment);
    }

    @Override
    public YamlMapping yamlMapping(final int index) {
        final YamlNode value = this.nodes.get(index);
        final YamlMapping found;
        if (value instanceof YamlMapping) {
            found = (YamlMapping) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public YamlSequence yamlSequence(final int index) {
        final YamlNode value = this.nodes.get(index);
        final YamlSequence found;
        if (value instanceof YamlSequence) {
            found = (YamlSequence) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public String string(final int index) {
        final YamlNode value = this.nodes.get(index);
        final String found;
        if (value instanceof PlainStringScalar) {
            found = ((PlainStringScalar) value).value();
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public String foldedBlockScalar(final int index) {
        final YamlNode value = this.nodes.get(index);
        final String found;
        if (value instanceof RtYamlScalarBuilder.BuiltFoldedBlockScalar) {
            found = ((RtYamlScalarBuilder.BuiltFoldedBlockScalar) value)
                .value();
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public Collection<String> literalBlockScalar(final int index) {
        final YamlNode value = this.nodes.get(index);
        final Collection<String> found;
        if (value instanceof RtYamlScalarBuilder.BuiltLiteralBlockScalar) {
            found = Arrays.asList(
                ((RtYamlScalarBuilder.BuiltLiteralBlockScalar) value)
                    .value()
                    .split(System.lineSeparator())
            );
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public Collection<YamlNode> values() {
        final List<YamlNode> children = new LinkedList<>();
        children.addAll(this.nodes);
        return children;
    }

    @Override
    public int size() {
        return this.nodes.size();
    }

    @Override
    public Iterator<YamlNode> iterator() {
        return this.nodes.iterator();
    }

    @Override
    public Comment comment() {
        return this.comment;
    }

}
