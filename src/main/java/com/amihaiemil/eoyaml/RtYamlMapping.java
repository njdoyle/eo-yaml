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
 * YAML mapping implementation (rt means runtime).
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * @see http://yaml.org/spec/1.2/spec.html#mapping//
 */
final class RtYamlMapping extends BaseYamlMapping {

    /**
     * Comments referring to this mapping.
     */
    private final Comment comment;

    /**
     * Comments referring the key:value pairs of this map.
     */
    private final Comments comments;

    /**
     * Key:value linked map (maintains the order of insertion).
     */
    private final Map<YamlNode, YamlNode> mappings =
        new LinkedHashMap<>();

    /**
     * Ctor.
     * @param entries Entries contained in this mapping.
     */
    RtYamlMapping(final Map<YamlNode, YamlNode> entries) {
        this(entries, new ArrayList<>(), "");
    }

    /**
     * Ctor.
     * @param entries Entries contained in this mapping.
     * @param keyComments Comments on top of the key: value pairs.
     * @param comment Comment on top of this YamlMapping.
     */
    RtYamlMapping(
        final Map<YamlNode, YamlNode> entries,
        final List<Comment> keyComments,
        final String comment
    ) {
        this.mappings.putAll(entries);
        this.comments = new RtComments(keyComments);
        this.comment = new BuiltComment(this, comment);
    }

    @Override
    public Set<YamlNode> keys() {
        final Set<YamlNode> keys = new LinkedHashSet<>();
        keys.addAll(this.mappings.keySet());
        return keys;
    }

    @Override
    public Collection<YamlNode> values() {
        return this.mappings.values();
    }

    @Override
    public YamlNode value(final YamlNode key) {
        return this.mappings.get(key);
    }

    @Override
    public Comment comment() {
        return this.comment;
    }

    @Override
    public Comments comments() {
        return this.comments;
    }

    @Override
    public YamlMapping yamlMapping(final YamlNode key) {
        final YamlNode value = this.mappings.get(key);
        final YamlMapping found;
        if (value != null && value instanceof YamlMapping) {
            found = (YamlMapping) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public YamlSequence yamlSequence(final YamlNode key) {
        final YamlNode value = this.mappings.get(key);
        final YamlSequence found;
        if (value != null && value instanceof YamlSequence) {
            found =  (YamlSequence) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public String string(final YamlNode key) {
        final YamlNode value = this.mappings.get(key);
        final String found;
        if (value != null && value instanceof PlainStringScalar) {
            found = ((Scalar) value).value();
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public String foldedBlockScalar(final YamlNode key) {
        final YamlNode value = this.mappings.get(key);
        final String found;
        if (value != null
            && value instanceof RtYamlScalarBuilder.BuiltFoldedBlockScalar
        ) {
            found = ((Scalar) value).value();
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public Collection<String> literalBlockScalar(final YamlNode key) {
        final YamlNode value = this.mappings.get(key);
        final Collection<String> found;
        if (value != null
            && value instanceof RtYamlScalarBuilder.BuiltLiteralBlockScalar
        ) {
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

}
