/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.cli;


import java.util.Iterator;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;

/**
 * Implementations of this interface are supposed to create a string
 * representation of ModelNode values formatted nicely.
 *
 * @author Alexey Loubyansky
 */
public interface ModelNodeFormatter {

    int OFFSET = 2;

    abstract class ModelNodeFormatterBase implements ModelNodeFormatter {
        @Override
        public void format(StringBuilder buf, int newLineOffset, ModelNode value) {
            if(value == null) {
                buf.append("null");
            } else if(!value.isDefined()) {
                buf.append("n/a");
            } else {
                formatDefined(buf, newLineOffset, value);
            }
        }

        abstract void formatDefined(StringBuilder buf, int newLineOffset, ModelNode value);
    }

    ModelNodeFormatterBase DEFAULT = new ModelNodeFormatterBase(){
        @Override
        public void formatDefined(StringBuilder buf, int newLineOffset, ModelNode value) {
            buf.append(value.toString());
        }
    };

    ModelNodeFormatterBase BOOLEAN = new ModelNodeFormatterBase(){
        @Override
        public void formatDefined(StringBuilder buf, int newLineOffset, ModelNode value) {
            buf.append(value.asBoolean());
        }
    };

    ModelNodeFormatterBase STRING = new ModelNodeFormatterBase(){
        @Override
        public void formatDefined(StringBuilder buf, int newLineOffset, ModelNode value) {
            buf.append(value.asString());
        }
    };

    ModelNodeFormatterBase LIST = new ModelNodeFormatterBase(){
        @Override
        void formatDefined(StringBuilder buf, int newLineOffset, ModelNode value) {
            final Iterator<ModelNode> iterator = value.asList().iterator();
            if(iterator.hasNext()) {
                ModelNode item = iterator.next();
                ModelNodeFormatterBase formatter = Factory.forType(item.getType());
                if(formatter == LIST) {
                    formatter.format(buf, newLineOffset + OFFSET, item);
                } else {
                    if (newLineOffset > 0) {
                        for (int i = 0; i < newLineOffset; ++i) {
                            buf.append(' ');
                        }
                    }
                    formatter.format(buf, newLineOffset, item);
                }
                while(iterator.hasNext()) {
                    item = iterator.next();
                    formatter = Factory.forType(item.getType());
                    buf.append('\n');
                    if(formatter == LIST) {
                        formatter.format(buf, newLineOffset + OFFSET, item);
                    } else {
                        if (newLineOffset > 0) {
                            for (int i = 0; i < newLineOffset; ++i) {
                                buf.append(' ');
                            }
                        }
                        formatter.format(buf, newLineOffset, item);
                    }
                }
            }
        }
    };

    ModelNodeFormatterBase PROPERTY = new ModelNodeFormatterBase(){
        @Override
        void formatDefined(StringBuilder buf, int newLineOffset, ModelNode value) {
            final Property prop = value.asProperty();
            buf.append(prop.getName());
            final ModelNode propValue = prop.getValue();
            final ModelNodeFormatterBase formatter = Factory.forType(propValue.getType());
            if(formatter == LIST) {
                buf.append('\n');
                formatter.format(buf, newLineOffset + OFFSET, propValue);
            } else {
                buf.append('=');
                formatter.format(buf, newLineOffset, propValue);
            }
        }
    };

    class Factory {
        public static ModelNodeFormatterBase forType(ModelType type) {
            if(type == ModelType.STRING) {
                return STRING;
            }
            if(type == ModelType.BOOLEAN) {
                return BOOLEAN;
            }
            if(type == ModelType.OBJECT || type == ModelType.LIST) {
                return LIST;
            }
            if(type == ModelType.PROPERTY) {
                return PROPERTY;
            }
            return DEFAULT;
        }

/*        public static void main(String[] args) throws Exception {

            ModelNode node = new ModelNode();
            node.get("text").set("story");

            ModelNode child = new ModelNode();
            child.get("one").set(1);
            child.get("two").set("deux");

            ModelNode list = new ModelNode();
            list.add().set("something");
            ModelNode n = new ModelNode();
            n.get("a").set("b");
            n.get("c").set("d");
            list.add().set(n);
            list.add().set("other");

            child.get("list").set(list);

            node.get("child").set(child);
            node.get("y").set("z");

            StringBuilder buf = new StringBuilder();
            Factory.forType(node.getType()).format(buf, 0, node);
            System.out.println(buf.toString());
        }
*/    }

    void format(StringBuilder buf, int offset, ModelNode value);
}
