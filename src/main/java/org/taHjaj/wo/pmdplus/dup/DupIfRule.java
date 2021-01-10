package org.taHjaj.wo.pmdplus.dup;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DupIfRule extends AbstractJavaRule {

    private final Set<String> violatingExpressions = new HashSet<>();

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

    private static <T> void findDirectDescendantsOfType(final Node node, final Class<? extends T> targetType, final List<T> results,
                                                        final List<Class<? extends JavaNode>> excludeJavaNodes) {
        for (Node child : node.children()) {
            if (targetType.isAssignableFrom(child.getClass())) {
                results.add(targetType.cast(child));
            } else if (!excludeJavaNodes.contains( child.getClass())) {
                findDirectDescendantsOfType(child, targetType, results, excludeJavaNodes);
            }
        }
    }


    @Override
    public void end(RuleContext ctx) {
        super.end(ctx);
    }

    private Map<String, List<JavaNode>> addViolations(ASTBlock astBlock, Object data) {
        final List<ASTIfStatement> astIfStatements = new ArrayList<>();

        findDirectDescendantsOfType(
                astBlock,
                ASTIfStatement.class,
                astIfStatements,
                false);

        Map<String, List<JavaNode>> expression2javanodes = new HashMap<>();

        final List<JavaNode> expressions = findAllRewritableExpressions(astBlock, Collections.singletonList(ASTIfStatement.class));

        astIfStatements.forEach(astIfStatement -> addViolations(astIfStatement, data, expression2javanodes));

        return expression2javanodes;

    }

    private void addViolations(ASTIfStatement astIfStatement, Object data, Map<String, List<JavaNode>> expression2javanodes) {
        // Determine the expressions in the branches
        JavaNode ifExpression = astIfStatement.getChild(0);
        List<JavaNode> ifExpressions = findAllRewritableExpressions(ifExpression, Collections.singletonList(ASTIfStatement.class));

        System.out.printf( "Expression %s contains the following expressions:%n", toString(ifExpression));

        addExpressions(expression2javanodes, ifExpressions);

        final Map<String, List<JavaNode>> thenJavaNodeExpression2JavaNodes = addViolations(((ASTBlock) astIfStatement.getThenBranch().getChild(0)), data);
        expression2javanodes.putAll( thenJavaNodeExpression2JavaNodes);

        final ASTStatement elseBranch = astIfStatement.getElseBranch();
        if( elseBranch != null) {
            final Map<String, List<JavaNode>> elseJavaNodeExpression2JavaNodes = addViolations(((ASTBlock) astIfStatement.getThenBranch().getChild(0)), data);
            expression2javanodes.putAll( elseJavaNodeExpression2JavaNodes);

            thenJavaNodeExpression2JavaNodes.forEach( (javaNodeExpressionString, javaNodes) -> {
                if( elseJavaNodeExpression2JavaNodes.containsKey(javaNodeExpressionString)) {
                    addMappings( expression2javanodes, javaNodeExpressionString, thenJavaNodeExpression2JavaNodes.get( javaNodeExpressionString));
                    addMappings( expression2javanodes, javaNodeExpressionString, elseJavaNodeExpression2JavaNodes.get( javaNodeExpressionString));
                }
            });
        }

//
//
//        for( int i=1; i<astIfStatement.getNumChildren(); i++) {
//            final JavaNode childNode = astIfStatement.getChild(i);
//            final List<JavaNode> astExpressionList = findAllRewritableExpressions(childNode, Collections.singletonList(ASTIfStatement.class));
//            branchExpressions.put( childNode, astExpressionList);
//        }
//
//        branchExpressions.forEach((key, value) -> {
//            System.out.printf("Branch %s contains the following expressions:%n", toString(key.getParent().getChild(0)));
//            value.forEach(expression ->
//                    System.out.printf("::: %s%n", toString(expression)));
//
//            addExpressions( expression2javanodes, value);
//        });
    }

    private void addExpressions(Map<String, List<JavaNode>> expression2javanodes, List<JavaNode> ifExpressions) {
        ifExpressions.forEach(javaNode -> {
            final String string = toString(javaNode);
            System.out.printf( "::: %s%n", string);

            addMappings(expression2javanodes, string, ifExpressions);
        });
    }

    private <K,V> void addMappings(Map<K, List<V>> key2Values, K key2, List<V> newValues) {
        key2Values.compute( key2, (key, values) -> {
            if( values == null) {
                values = new ArrayList<>();
            }

            values.addAll(newValues);

            return values;
        });
    }

    private <K,V> void addMapping(Map<K, List<V>> key2Values, K key2, V newValue) {
        key2Values.compute( key2, (key, values) -> {
            if( values == null) {
                values = new ArrayList<>();
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

        astBlocks.forEach(astBlock -> {
            final Map<String, List<JavaNode>> expression2javanodes = addViolations(astBlock, data);
            expression2javanodes.forEach((key, value) -> reportDuplicates(data, key, value));
        });


        return super.visit(astMethodDeclaration, data);
    }

//    @Override
//    public Object visit(ASTIfStatement node, Object data) {
//
//            // Determine the expression in the if statement.
//        // Determine the expressions in the branches
//        JavaNode ifExpression = node.getChild(0);
//        List<JavaNode> ifExpressions = findAllRewritableExpressions(ifExpression);
//
//        System.out.printf( "Expression %s contains the following expressions:%n", toString(ifExpression));
//        ifExpressions.forEach(javaNode -> System.out.printf( "::: %s%n", toString(javaNode)));
//
//        Map<JavaNode, List<JavaNode>> branchExpressions = new HashMap<>();
//
//        for( int i=1; i<node.getNumChildren(); i++) {
//            final JavaNode childNode = node.getChild(i);
//            final List<JavaNode> astExpressionList = findAllRewritableExpressions(childNode);
//            branchExpressions.put( childNode, astExpressionList);
//        }
//
//        branchExpressions.forEach((key, value) -> {
//            System.out.printf("Branch %s contains the following expressions:%n", toString(key.getParent().getChild(0)));
//            value.forEach(expression ->
//                    System.out.printf("::: %s%n", toString(expression)));
//        });
//
//
//        // Now check if there is any expression in the branches that is compatible
//        // with the ifExpression
//
//
//        reportDuplicates(ifExpressions, branchExpressions, data);
//
//        return super.visit(node, data);
//    }

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
        List<JavaNode> astRelationalExpressions = new ArrayList<>();
        findDirectDescendantsOfType(node, ASTRelationalExpression.class, astRelationalExpressions, excludeJavaNodes);
        for( JavaNode astRelationalExpression : astRelationalExpressions) {
            results.addAll(findAllRewritableExpressions(astRelationalExpression.getChild(0)));
            results.addAll(findAllRewritableExpressions(astRelationalExpression.getChild(1)));
        }

        findDirectDescendantsOfType(node, ASTExpression.class, results, excludeJavaNodes);

        if(isExpression(node)) {
            results.add(node);
        } else if( node instanceof ASTPrimaryExpression) {
            if( node.getNumChildren() == 2
                    && node.getChild(0) instanceof ASTPrimaryPrefix
                    && node.getChild(1) instanceof ASTPrimarySuffix) {
                results.add(node);
                results.addAll(findAllRewritableExpressions(node.getChild(1)));
            }
        }
        return results;
    }

    private List<JavaNode> findAllExpressions(JavaNode node) {
        final List<JavaNode> astExpressionList = node
                .findDescendantsOfType(ASTExpression.class);

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
                || node instanceof ASTAndExpression;
    }


    private void reportDuplicates(
            List<JavaNode> ifExpressions,
            Map<JavaNode, List<JavaNode>> branchExpressions, Object data) {
        reportDuplicatesOnIfStatement(ifExpressions, branchExpressions, data);

        branchExpressions.forEach((key, value1) -> {
            reportDuplicates(data, key, value1);
        });
    }

    private void reportDuplicates(Object data, JavaNode key, List<JavaNode> value1) {
        Map<String, List<JavaNode>> frequency = new HashMap<>();

        value1.forEach(javaNode -> {
            String value = toString(javaNode);

            frequency.compute(value, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }

                v.add(javaNode);

                return v;
            });
        });

        frequency.forEach((key1, value2) -> {
            if (value2.size() > 1) {
                addViolation(key, data, value2, key1);
            }
        });
    }

    private void reportDuplicates(Object data, String key, List<JavaNode> value1) {
        Map<String, List<JavaNode>> frequency = new HashMap<>();

        value1.forEach(javaNode -> {
            String value = toString(javaNode);

            frequency.compute(value, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }

                v.add(javaNode);

                return v;
            });
        });

        frequency.forEach((key1, value2) -> {
            if (value2.size() > 1) {
                addViolation(key, data, value2, key1);
            }
        });
    }

    private void reportDuplicatesOnIfStatement(List<JavaNode> ifExpressions, Map<JavaNode, List<JavaNode>> branchExpressions, Object data) {
        ifExpressions.forEach(ifE -> branchExpressions.entrySet().stream().flatMap(e -> e.getValue().stream()).forEach(statementExpression -> {
            final String ifExpressionString = toString(ifE);
            final String statementExpressionString = toString(statementExpression);
            if (ifExpressionString.equals(statementExpressionString)) {
                addViolation(ifE, data, Arrays.asList(ifE, statementExpression), toString(ifE));
            }
        }));
    }

    private String toString(JavaNode javaNode) {
        StringBuilder stringBuilder = new StringBuilder();

        toString( stringBuilder, javaNode);

//        toString(stringBuilder, javaNode.getChild(0));
//
//        if( javaNode.getNumChildren() > 1) {
//            for( int i=1; i<javaNode.getNumChildren(); i++) {
//                toString(stringBuilder, javaNode.getChild(i));
//            }
//        }

        return stringBuilder.toString();
    }

    private void toString(StringBuilder stringBuilder, JavaNode javaNode) {

        if( javaNode instanceof ASTExpression) {
            stringBuilder.append('(');
            toString(stringBuilder, javaNode.getChild(0));
            stringBuilder.append(')');
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
        } else if( javaNode instanceof ASTAndExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            final String image = javaNode.getImage();
            stringBuilder.append(image);
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTArgumentList) {
            for (JavaNode child : javaNode.children()) {
                toString(stringBuilder, child);
            }
        } else if( javaNode instanceof ASTPrimaryExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            if( javaNode.getNumChildren() > 1) {
                toString(stringBuilder, javaNode.getChild(1));
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
                    if (javaNode instanceof ASTArguments) {
                        stringBuilder.append("()");
                    } else {
                        stringBuilder.append("unkonwn javaNode ").append(javaNode.getClass().getTypeName());
                    }
                } else {
                    stringBuilder.append(image);
                }
            }
        }
    }

    private void addViolation(final JavaNode javaNode, final Object data, final List<JavaNode> javaNodes,
                              final String expressionString) {
        if( ! violatingExpressions.contains( expressionString)) {
            violatingExpressions.add( expressionString);
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
            addViolation(data, javaNode, new String[]{expressionString,
                    lines});
        }
    }

    private void addViolation(final String javaNode, final Object data, final List<JavaNode> javaNodes,
                              final String expressionString) {
        if( ! violatingExpressions.contains( expressionString)) {
            violatingExpressions.add( expressionString);
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
    }

    private String lineAndColumn(JavaNode javaNode) {
        return javaNode.getBeginLine() + ":" + javaNode.getBeginColumn();
    }
}
