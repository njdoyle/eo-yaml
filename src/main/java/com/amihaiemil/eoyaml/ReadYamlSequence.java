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
 * YamlSequence read from somewhere.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
final class ReadYamlSequence extends BaseYamlSequence {

    /**
     * Yaml line just previous to the one where this sequence starts. E.g.
     * <pre>
     * 0  sequence:
     * 1    - elem1
     * 2    - elem2
     * </pre>
     * In the above example the sequence consists of elem1 and elem2, while
     * "previous" is line 0. If the sequence starts at the root, then line
     * "previous" is {@link com.amihaiemil.eoyaml.YamlLine.NullYamlLine}; E.g.
     * <pre>
     * 0  - elem1
     * 1  - elem2
     * </pre>
     */
    private final YamlLine previous;

    /**
     * All lines of the YAML document.
     */
    private final AllYamlLines all;

    /**
     * Only the significant lines of this sequence.
     */
    private final YamlLines significant;

    /**
     * Ctor.
     * @param lines Given lines.
     */
    ReadYamlSequence(final AllYamlLines lines) {
        this(new YamlLine.NullYamlLine(), lines);
    }

    /**
     * Ctor.
     * @param previous Line just before the start of this sequence.
     * @param lines Given lines.
     */
    ReadYamlSequence(final YamlLine previous, final AllYamlLines lines) {
        this.previous = previous;
        this.all = lines;
        this.significant = new SameIndentationLevel(
            new WellIndented(
                new Skip(
                    lines,
                    line -> line.number() <= previous.number(),
                    line -> line.trimmed().startsWith("#"),
                    line -> line.trimmed().startsWith("---"),
                    line -> line.trimmed().startsWith("..."),
                    line -> line.trimmed().startsWith("%"),
                    line -> line.trimmed().startsWith("!!")
                )
            )
        );
    }

    @Override
    public Collection<YamlNode> values() {
        final List<YamlNode> kids = new LinkedList<>();
        for(final YamlLine line : this.significant) {
            final String trimmed = line.trimmed();
            if("-".equals(trimmed)
                || trimmed.endsWith("|")
                || trimmed.endsWith(">")
            ) {
                kids.add(this.significant.toYamlNode(line));
            } else {
                kids.add(new ReadPlainScalar(this.all, line));
            }
        }
        return kids;
    }

    @Override
    public YamlMapping yamlMapping(final int index) {
        YamlMapping mapping = null;
        int count = 0;
        for (final YamlNode node : this.values()) {
            if (count == index && node instanceof YamlMapping) {
                mapping = (YamlMapping) node;
            }
            count = count + 1;
        }
        return mapping;
    }

    @Override
    public YamlSequence yamlSequence(final int index) {
        YamlSequence sequence = null;
        int count = 0;
        for (final YamlNode node : this.values()) {
            if (count == index && node instanceof YamlSequence) {
                sequence = (YamlSequence) node;
            }
            count = count + 1;
        }
        return sequence;
    }

    @Override
    public String string(final int index) {
        String value = null;
        int count = 0;
        for (final YamlNode node : this.values()) {
            if(count == index && (node instanceof ReadPlainScalar)) {
                value = ((Scalar) node).value();
                break;
            }
            count++;
        }
        return value;
    }

    @Override
    public String foldedBlockScalar(final int index) {
        String value = null;
        int count = 0;
        for (final YamlNode node : this.values()) {
            if(count == index && (node instanceof ReadFoldedBlockScalar)) {
                value = ((Scalar) node).value();
                break;
            }
            count++;
        }
        return value;
    }

    @Override
    public Collection<String> literalBlockScalar(final int index) {
        Collection<String> value = null;
        int count = 0;
        for (final YamlNode node : this.values()) {
            if(count == index && (node instanceof ReadLiteralBlockScalar)) {
                value = Arrays.asList(
                    ((ReadLiteralBlockScalar) node)
                        .value().split(System.lineSeparator())
                );
                break;
            }
            count++;
        }
        return value;
    }

    @Override
    @SuppressWarnings("unused")
    public int size() {
        int size = 0;
        for(final YamlLine line : this.significant) {
            size = size + 1;
        }
        return size;
    }

    @Override
    public Iterator<YamlNode> iterator() {
        return this.values().iterator();
    }

    /**
     * Return the comment referring to this YamlSequence.
     * @todo #276:30min Continue implementing support for Comments into the
     *  read version of YamlMapping. Comments for the elements of a sequence
     *  are already implemented in BaseYamlSequence.
     * @return Comment.
     */
    @Override
    public Comment comment() {
        return new ReadComment(
            new FirstCommentFound(
                new Backwards(
                    new Skip(
                        this.all,
                        line -> {
                            final boolean skip;
                            if(this.previous.number() < 0) {
                                if(this.significant.iterator().hasNext()) {
                                    skip = line.number() >= this.significant
                                            .iterator().next().number();
                                } else {
                                    skip = false;
                                }
                            } else {
                                skip = line.number() >= this.previous.number();
                            }
                            return skip;
                        },
                        line -> line.trimmed().startsWith("---"),
                        line -> line.trimmed().startsWith("..."),
                        line -> line.trimmed().startsWith("%"),
                        line -> line.trimmed().startsWith("!!")
                    )
                )
            ),
            this
        );
    }

}
