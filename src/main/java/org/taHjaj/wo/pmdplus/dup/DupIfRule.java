package org.taHjaj.wo.pmdplus.dup;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DupIfRule extends AbstractJavaRule {

    private static <T> void findDirectDescendantsOfType(final Node node, final Class<? extends T> targetType, final List<T> results,
                                                        final boolean crossFindBoundaries) {
        for (Node child : node.children()) {
            if (targetType.isAssignableFrom(child.getClass())) {
                results.add(targetType.cast(child));
            } else if (crossFindBoundaries || !child.isFindBoundary()) {
                findDirectDescendantsOfType(child, targetType, results, crossFindBoundaries);
            }
        }
    }

    private static <T> void findDescendantsOfType(final Node node, final Class<? extends T> targetType, final List<T> results,
                                                        final List<Class<? extends JavaNode>> excludeJavaNodes) {
        for (Node child : node.children()) {
            if (!excludeJavaNodes.contains( child.getClass())) {
                if (targetType.isAssignableFrom(child.getClass())) {
                    results.add(targetType.cast(child));
                }
                findDescendantsOfType(child, targetType, results, excludeJavaNodes);
            }
        }
    }

    private void addViolations(Map<String, Set<JavaNode>> expression2javanodes, Map<String, Set<JavaNode>> ifExpression2javanodes, Map<String, Set<JavaNode>> violatingExpressions, ASTBlock astBlock, Object data) {
        final List<ASTIfStatement> astIfStatements = new ArrayList<>();

        findDirectDescendantsOfType(
                astBlock,
                ASTIfStatement.class,
                astIfStatements,
                false);

        // Find all expressions in the current block, but exclude if statements
        final List<JavaNode> expressions = findAllRewritableExpressions(astBlock, Collections.singletonList(ASTIfStatement.class));

        final List<ASTIfStatement> astIfElseStatements = new ArrayList<>();

        // include if statements, but not then and else branches
        astIfStatements.forEach(astIfStatement ->

            {
                astIfElseStatements.add( astIfStatement);
                expressions.addAll(
                        findAllRewritableExpressions(astIfStatement.getChild(0), Collections.singletonList(ASTIfStatement.class)));
//                if( astIfStatement.getNumChildren() == 3) {
//                    final JavaNode child = astIfStatement.getChild(2);
//
//                    if( child.getNumChildren() == 1) {
//                        final JavaNode grandChild = child.getChild(0);
//                        if( grandChild instanceof ASTIfStatement) {
//                            astIfElseStatements.add( (ASTIfStatement)grandChild);
//                            expressions.addAll(
//                                    findAllRewritableExpressions(grandChild.getChild(0), Collections.singletonList(ASTIfStatement.class)));
//                        }
//                    }
//                }
            }
        );

        dump(expressions);

        dump(expression2javanodes);

        // Now collect all expressions in the if statements, add any violation if found
        astIfElseStatements.forEach(astIfStatement -> {
            Map<String, Set<JavaNode>> ifExpressionInstance2javanodes = new HashMap<>();

            addViolations(ifExpressionInstance2javanodes, ifExpression2javanodes, violatingExpressions, astIfStatement, data);

            expressions.forEach(javaNode -> {
                final String key = toString(javaNode);
                if (ifExpressionInstance2javanodes.containsKey(key)) {
                    // Violation!
                    addMappings(violatingExpressions, key, ifExpressionInstance2javanodes.get(key));
                    addMapping(violatingExpressions, key, javaNode);
                }
                if (ifExpression2javanodes.containsKey(key)) {
                    // Violation!
                    addMappings(violatingExpressions, key, ifExpression2javanodes.get(key));
                    addMapping(violatingExpressions, key, javaNode);
                }
            });

            ifExpressionInstance2javanodes.forEach((key, value) -> addMappings(ifExpression2javanodes, key, value));
        });

        // Now find any double occurrence of an expression; add it as a violation.
        expressions.forEach(javaNode -> {
            final String key = toString(javaNode);
            if( expression2javanodes.containsKey( key)) {
                // Violation!
                addMappings(violatingExpressions, key, expression2javanodes.get(key));
                addMapping(violatingExpressions, key, javaNode);
            }
            addMapping(expression2javanodes, key, javaNode);
        });
    }

    private void dump(Map<String, Set<JavaNode>> expression2javanodes) {
        expression2javanodes.forEach((key, value) -> {
            System.out.printf("%s%n", key);
            dump(value);
        });
    }

    private void dump(Collection<JavaNode> expressions) {
        expressions.forEach(javaNode -> System.out.printf( "::: %s\t(%d,%d)%n", toString(javaNode), javaNode.getBeginLine(), javaNode.getBeginColumn()));
    }

    private void addViolations(Map<String, Set<JavaNode>> expression2javanodes, Map<String, Set<JavaNode>> ifExpression2javanodes, Map<String, Set<JavaNode>> violatingExpressions, ASTIfStatement astIfStatement, Object data) {
        // Determine the expressions in the branches
        final ASTStatement elseBranch = astIfStatement.getElseBranch();
        if( elseBranch == null) {
            addViolations( expression2javanodes, ifExpression2javanodes, violatingExpressions, ((ASTBlock) astIfStatement.getThenBranch().getChild(0)), data);
        } else {
            Map<String, Set<JavaNode>> thenExpression2javanodes = new HashMap<>();
            addViolations( thenExpression2javanodes, ifExpression2javanodes, violatingExpressions, ((ASTBlock) astIfStatement.getThenBranch().getChild(0)), data);

            Map<String, Set<JavaNode>> elseExpression2javanodes = new HashMap<>();
            final JavaNode child = astIfStatement.getElseBranch().getChild(0);
            if( child instanceof ASTBlock) {
                addViolations(elseExpression2javanodes, ifExpression2javanodes, violatingExpressions, ((ASTBlock) child), data);

                thenExpression2javanodes.forEach((javaNodeExpressionString, javaNodes) -> {
                    if (elseExpression2javanodes.containsKey(javaNodeExpressionString)) {
                        addMappings(expression2javanodes, javaNodeExpressionString, thenExpression2javanodes.get(javaNodeExpressionString));
                        addMappings(expression2javanodes, javaNodeExpressionString, elseExpression2javanodes.get(javaNodeExpressionString));
                        addMappings(violatingExpressions, javaNodeExpressionString, thenExpression2javanodes.get(javaNodeExpressionString));
                        addMappings(violatingExpressions, javaNodeExpressionString, elseExpression2javanodes.get(javaNodeExpressionString));
                    }
                });
            } else {
                addViolations(elseExpression2javanodes, ifExpression2javanodes, violatingExpressions, ((ASTIfStatement) child), data);

                thenExpression2javanodes.forEach((javaNodeExpressionString, javaNodes) -> {
                    if (elseExpression2javanodes.containsKey(javaNodeExpressionString)) {
                        addMappings(expression2javanodes, javaNodeExpressionString, thenExpression2javanodes.get(javaNodeExpressionString));
                        addMappings(expression2javanodes, javaNodeExpressionString, elseExpression2javanodes.get(javaNodeExpressionString));
                        addMappings(violatingExpressions, javaNodeExpressionString, thenExpression2javanodes.get(javaNodeExpressionString));
                        addMappings(violatingExpressions, javaNodeExpressionString, elseExpression2javanodes.get(javaNodeExpressionString));
                    }
                });
            }
        }
    }

    private <K,V> void addMappings(Map<K, Set<V>> key2Values, K key2, Set<V> newValues) {
        key2Values.compute( key2, (key, values) -> {
            if( values == null) {
                values = new HashSet<>();
            }

            values.addAll(newValues);

            return values;
        });
    }

    private <K,V> void addMapping(Map<K, Set<V>> key2Values, K key2, V newValue) {
        key2Values.compute( key2, (key, values) -> {
            if( values == null) {
                values = new HashSet<>();
            }

            values.add(newValue);

            return values;
        });
    }

    @Override
    public Object visit(final ASTMethodDeclaration astMethodDeclaration, final Object data) {
        final List<ASTBlock> astBlocks = new ArrayList<>();

        findDirectDescendantsOfType(
                astMethodDeclaration,
                ASTBlock.class,
                astBlocks,
                false);

        final Map<String, Set<JavaNode>> expression2javanodes = new HashMap<>();
        final Map<String, Set<JavaNode>> ifExpression2javanodes = new HashMap<>();
        final Map<String, Set<JavaNode>> violatingExpressions = new HashMap<>();
        astBlocks.forEach(astBlock -> addViolations(expression2javanodes, ifExpression2javanodes, violatingExpressions, astBlock, data));

        // Add any ifExpression2javanodes to violationExpressions
        violatingExpressions.forEach((key, values) -> {
            if( ifExpression2javanodes.containsKey(key)) {
                addMappings( violatingExpressions, key, values);
            }
        });

        violatingExpressions.forEach((s, javaNodes) -> reportViolation(data, javaNodes, s));

        return super.visit(astMethodDeclaration, data);
    }

    private List<JavaNode> findAllRewritableExpressions(JavaNode node) {
        final List<JavaNode> javaNodes = findAllExpressions(node);

        return javaNodes.stream().filter(this::isRewritable).collect(Collectors.toList());
    }

    private List<JavaNode> findAllRewritableExpressions(JavaNode node, List<Class<? extends JavaNode>> excludeJavaNodes) {
        final List<JavaNode> javaNodes = findAllExpressions(node, excludeJavaNodes);

        return javaNodes.stream().filter(this::isRewritable).collect(Collectors.toList());
    }

    private boolean isRewritable(JavaNode javaNode) {
        if( javaNode.getNumChildren() == 1) {
            // PrimaryExpression
            final JavaNode primaryExpression = javaNode.getChild(0);
            if(primaryExpression instanceof ASTPrimaryExpression && primaryExpression.getNumChildren() == 1) {
                // PrimaryPrefix
                final JavaNode primaryPrefix = primaryExpression.getChild(0);
                if( primaryPrefix instanceof  ASTPrimaryPrefix && primaryPrefix.getNumChildren() == 1) {
                    // Name
                    final JavaNode branchJavaNode = primaryPrefix.getChild(0);
                    return !(branchJavaNode instanceof ASTName) && !(branchJavaNode instanceof ASTLiteral);
                }
            }
        }

        return true;
    }

    private List<JavaNode> findAllExpressions(JavaNode node, List<Class<? extends JavaNode>> excludeJavaNodes) {

        List<JavaNode> results = new ArrayList<>();
        final boolean present = excludeJavaNodes.stream().findFirst().filter(aClass -> node.getClass().isAssignableFrom(aClass)).isPresent();
        System.out.printf( "Processing node of type %s: %s%n",
                node.getClass().getCanonicalName(), node.getImage());
        if( !present) {
            if (node instanceof ASTBlock
                    || node instanceof ASTBlockStatement
                    || node instanceof ASTForStatement
                    || node instanceof ASTForInit
                    || node instanceof ASTForUpdate
                    || node instanceof ASTLocalVariableDeclaration
                    || node instanceof ASTStatement
                    || node instanceof ASTStatementExpression
                    || node instanceof ASTStatementExpressionList
                    || node instanceof ASTReturnStatement
                    || node instanceof ASTType
                    || node instanceof ASTVariableDeclarator) {
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTConditionalExpression) {
                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTAllocationExpression) {
                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTExpression) {
//                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTPrimaryExpression) {
                if (node.getNumChildren() == 2
                        && node.getChild(0) instanceof ASTPrimaryPrefix
                        && node.getChild(1) instanceof ASTPrimarySuffix) {
                    results.add(node);
                    results.addAll(findAllRewritableExpressions(node.getChild(1), excludeJavaNodes));
                } else if (node.getNumChildren() == 1) {
                    results.addAll(findAllRewritableExpressions(node.getChild(0), excludeJavaNodes));
                }
            } else if (node instanceof ASTName) {
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTPrimarySuffix) {
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTArguments) {
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTConditionalAndExpression) {
                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTConditionalOrExpression) {
                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTPrimaryPrefix) {
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTAndExpression) {
                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTLiteral) {
                System.out.printf("Literal %s%n", node.getImage());
//                results.add(node);
            } else if (node instanceof ASTRelationalExpression) {
//                List<JavaNode> astRelationalExpressions = new ArrayList<>();
//                findDirectDescendantsOfType(node, ASTRelationalExpression.class, astRelationalExpressions, excludeJavaNodes);
//                for (JavaNode astRelationalExpression : astRelationalExpressions) {
                results.add(node);
                results.addAll(findAllRewritableExpressions(node.getChild(0)));
                results.addAll(findAllRewritableExpressions(node.getChild(1)));
//                }
            } else if (node instanceof ASTEqualityExpression) {
                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTAdditiveExpression) {
                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTMultiplicativeExpression) {
                results.add(node);
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else if (node instanceof ASTArgumentList) {
                node.children().forEach(javaNode -> results.addAll(findAllExpressions(javaNode, excludeJavaNodes)));
            } else {
                System.out.printf( "Not really processing node of type %s: %s%n",
                        node.getClass().getCanonicalName(), node.getImage());


                if (node.getNumChildren() == 1) {
                    final JavaNode astConditionalAndExpression = node.getChild(0);
                    if (astConditionalAndExpression instanceof ASTConditionalAndExpression) {
                        results.addAll(findAllRewritableExpressions(astConditionalAndExpression.getChild(0)));
                        results.addAll(findAllRewritableExpressions(astConditionalAndExpression.getChild(1)));
                    }
                }

                findDescendantsOfType(node, ASTExpression.class, results, excludeJavaNodes);

                if (isExpression(node)) {
                    results.add(node);
                }
            }
        }
        return results;
    }

    private List<JavaNode> findAllExpressions(JavaNode node) {
        final List<JavaNode> astExpressionList = new ArrayList<>();

        final List<ASTRelationalExpression> astRelationalExpressions =
                node.findDescendantsOfType(ASTRelationalExpression.class);

        for( ASTRelationalExpression astRelationalExpression : astRelationalExpressions) {
            astExpressionList.addAll(findAllRewritableExpressions(astRelationalExpression.getChild(0)));
            astExpressionList.addAll(findAllRewritableExpressions(astRelationalExpression.getChild(1)));
        }

        if(isExpression(node)) {
            astExpressionList.add(node);
        } else if( node instanceof ASTPrimaryExpression) {
            if( node.getNumChildren() == 2
                    && node.getChild(0) instanceof ASTPrimaryPrefix
                    && node.getChild(1) instanceof ASTPrimarySuffix) {
                astExpressionList.add(node);
                astExpressionList.addAll(findAllRewritableExpressions(node.getChild(1)));
            }

        }
        return astExpressionList;
    }

    private boolean isExpression(Node node) {
        return node instanceof ASTExpression
                || node instanceof ASTAdditiveExpression
                || node instanceof ASTConditionalAndExpression
                || node instanceof ASTConditionalOrExpression
                || node instanceof ASTEqualityExpression
                || node instanceof ASTAndExpression
//                || node instanceof ASTLiteral
                ;
    }


    private String toString(JavaNode javaNode) {
        StringBuilder stringBuilder = new StringBuilder();

        toString(stringBuilder, javaNode);

        return stringBuilder.toString();
    }

    private void toString(StringBuilder stringBuilder, JavaNode javaNode) {

        if( javaNode instanceof ASTExpression) {
//            stringBuilder.append('(');
            toString(stringBuilder, javaNode.getChild(0));
//            stringBuilder.append(')');
        } else if( javaNode instanceof ASTPrimaryPrefix) {
            if( javaNode.getChild(0) instanceof ASTName
            || javaNode.getChild(0) instanceof  ASTLiteral) {
                toString(stringBuilder, javaNode.getChild(0));
            } else {
                stringBuilder.append('(');
                toString(stringBuilder, javaNode.getChild(0));
                stringBuilder.append(')');
            }
        } else if( javaNode instanceof ASTPrimaryExpression) {
            if( javaNode.getNumChildren() > 1) {
//                stringBuilder.append('(');
                toString(stringBuilder, javaNode.getChild(0));
                toString(stringBuilder, javaNode.getChild(1));
//                stringBuilder.append(')');
            } else {
                toString(stringBuilder, javaNode.getChild(0));
            }
        } else if( javaNode instanceof ASTConditionalExpression) {
            if( javaNode.getNumChildren() == 3) {
                toString(stringBuilder, javaNode.getChild(0));
                stringBuilder.append("?");
                toString(stringBuilder, javaNode.getChild(1));
                stringBuilder.append(":");
                toString(stringBuilder, javaNode.getChild(2));
            } else {
                stringBuilder.append('(');
                toString(stringBuilder, javaNode.getChild(0));
                stringBuilder.append(')');
            }
        } else if( javaNode instanceof ASTAllocationExpression) {
            stringBuilder.append("new ");
            toString(stringBuilder, javaNode.getChild(0));
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTRelationalExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            final String image = javaNode.getImage();
            stringBuilder.append(image);
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTAdditiveExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            final String image = javaNode.getImage();
            stringBuilder.append(image);
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTConditionalAndExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            stringBuilder.append("&&");
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTConditionalOrExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            stringBuilder.append("||");
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTClassOrInterfaceType) {
            stringBuilder.append( javaNode.getImage());
        } else if( javaNode instanceof ASTTypeArguments) {
            stringBuilder.append("<");
            final int numChildren = javaNode.getNumChildren();
            if(javaNode.getNumChildren() > 0) {
                toString(stringBuilder, javaNode.getChild(0));
                for (int i = 1; i < numChildren; i++) {
                    stringBuilder.append(',');
                    toString(stringBuilder, javaNode.getChild(0));
                }
            }
            stringBuilder.append(">");
        } else if( javaNode instanceof ASTAndExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            stringBuilder.append("&");
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTMultiplicativeExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            stringBuilder.append("*");
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTEqualityExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            final String image = javaNode.getImage();
            stringBuilder.append(image);
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTArgumentList) {
            final int numChildren = javaNode.getNumChildren();
            if( numChildren > 0) {
                toString( stringBuilder, javaNode.getChild(0));
                if( numChildren > 1) {
                    for( int i=1; i<numChildren; i++) {
                        stringBuilder.append(',');
                        toString( stringBuilder, javaNode.getChild(i));
                    }
                }
            }
        } else if( javaNode instanceof ASTArguments) {
            stringBuilder.append('(');
            for (JavaNode child : javaNode.children()) {
                toString(stringBuilder, child);
            }
            stringBuilder.append(')');
        } else if ( javaNode instanceof ASTPrimarySuffix) {
            ASTPrimarySuffix astPrimarySuffix = (ASTPrimarySuffix) javaNode;
            if( astPrimarySuffix.isArrayDereference()) {
                stringBuilder.append( '[');
                for (JavaNode child : javaNode.children()) {
                    toString(stringBuilder, child);
                }
                stringBuilder.append( ']');
            } else {
//                stringBuilder.append( '(');
                for (JavaNode child : javaNode.children()) {
                    toString(stringBuilder, child);
                }
//                stringBuilder.append( ')');
            }
       } else {
            final int numChildren = javaNode.getNumChildren();
            if (numChildren > 0) {
                for (JavaNode child : javaNode.children()) {
                    toString(stringBuilder, child);
                }
            } else {
                final String image = javaNode.getImage();
                if (image == null) {
                    if (javaNode instanceof ASTNullLiteral) {
                        stringBuilder.append("null");
                    } else {
                        stringBuilder.append("unkonwn javaNode ").append(javaNode.getClass().getTypeName());
                    }
                } else {
                    stringBuilder.append(image);
                }
            }
        }
    }

    private void reportViolation(final Object data, final Set<JavaNode> violationJavaNodes,
                              final String expressionString) {

        List<JavaNode> javaNodes = new ArrayList<>(violationJavaNodes);

        javaNodes.sort((javaNode1, javaNode2) -> {
            final int beginLine1 = javaNode1.getBeginLine();
            final int beginLine2 = javaNode2.getBeginLine();
            if( beginLine1 < beginLine2) {
                return -1;
            } else if ( beginLine1 == beginLine2) {
                final int beginColumn1 = javaNode1.getBeginColumn();
                final int beginColumn2 = javaNode2.getBeginColumn();
                return Integer.compare(beginColumn1, beginColumn2);
            } else {
                return 1;
            }
        });
        final String lines = StringUtils
                .join(javaNodes.stream().map(this::lineAndColumn).collect(Collectors.toList()), ",");
        addViolation(data, javaNodes.get(0), new String[]{expressionString,
                lines});
    }

    private String lineAndColumn(JavaNode javaNode) {
        return javaNode.getBeginLine() + ":" + javaNode.getBeginColumn();
    }
}
