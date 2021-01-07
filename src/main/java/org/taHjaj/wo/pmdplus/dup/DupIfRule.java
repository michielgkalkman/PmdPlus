package org.taHjaj.wo.pmdplus.dup;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DupIfRule extends AbstractJavaRule {

    private Set<String> violatingExpressions = new HashSet<>();

    @Override
    public Object visit(final ASTMethodDeclaration node, final Object data) {

        final List<JavaNode> expressions = findAllRewritableExpressions(node.getBody());

        reportDuplicates( data, node, expressions);
        
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTIfStatement node, Object data) {

        // Determine the expression in the if statement.
        // Determine the expressions in the branches
        JavaNode ifExpression = node.getChild(0);
        List<JavaNode> ifExpressions = findAllRewritableExpressions(ifExpression);

        System.out.printf( "Expression %s contains the following expressions:%n", toString(ifExpression));
        ifExpressions.forEach(javaNode -> System.out.printf( "::: %s%n", toString(javaNode)));

        Map<JavaNode, List<JavaNode>> branchExpressions = new HashMap<>();

        for( int i=1; i<node.getNumChildren(); i++) {
            final JavaNode childNode = node.getChild(i);
            final List<JavaNode> astExpressionList = findAllRewritableExpressions(childNode);
            branchExpressions.put( childNode, astExpressionList);
        }

        branchExpressions.forEach((key, value) -> {
            System.out.printf("Branch %s contains the following expressions:%n", toString(key.getParent().getChild(0)));
            value.forEach(expression ->
                    System.out.printf("branchExpression: %s%n", toString(expression)));
        });


        // Now check if there is any expression in the branches that is compatible
        // with the ifExpression


        reportDuplicates(ifExpressions, branchExpressions, data);

        return super.visit(node, data);
    }

    private List<JavaNode> findAllRewritableExpressions(JavaNode node) {
        final List<JavaNode> javaNodes = findAllExpressions(node);

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

    private void reportDuplicatesOnIfStatement(List<JavaNode> ifExpressions, Map<JavaNode, List<JavaNode>> branchExpressions, Object data) {
        ifExpressions.forEach(ifE -> branchExpressions.entrySet().stream().flatMap(e -> e.getValue().stream()).forEach(statementExpression -> {
            final String ifExpressionString = toString(ifE);
            final String statementExpressionString = toString(statementExpression);
            System.out.printf("Compare %s with %s%n", ifExpressionString, statementExpressionString);
            if (ifExpressionString.equals(statementExpressionString)) {
                System.out.printf("Equal %s with %s%n", ifExpressionString, statementExpressionString);
                addViolation(ifE, data, Arrays.asList(ifE, statementExpression), toString(ifE));
            } else {
                System.out.printf("Not equal %s with %s%n", ifExpressionString, statementExpressionString);
            }
        }));
    }

    private String toString(JavaNode javaNode) {
        StringBuilder stringBuilder = new StringBuilder();

        toString(stringBuilder, javaNode.getChild(0));

        if( javaNode.getNumChildren() > 1) {
            for( int i=1; i<javaNode.getNumChildren(); i++) {
                toString(stringBuilder, javaNode.getChild(i));
            }
        }

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
                    if( beginColumn1 < beginColumn2) {
                        return -1;
                    } else if ( beginColumn1 == beginColumn2) {
                        return 0;
                    } else {
                        return 1;
                    }
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

    private String lineAndColumn(JavaNode javaNode) {
        return javaNode.getBeginLine() + ":" + javaNode.getBeginColumn();
    }
}
