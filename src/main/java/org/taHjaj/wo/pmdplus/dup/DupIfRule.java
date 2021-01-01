package org.taHjaj.wo.pmdplus.dup;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DupIfRule extends AbstractJavaRule {
    @Override
    public Object visit(ASTIfStatement node, Object data) {

        // Determine the expression in the if statement.
        // Determine the expressions in the branches
        JavaNode ifExpression;
        List<JavaNode> ifExpressions = new ArrayList<>();
        Map<Node, List<JavaNode>> branchExpressions = new HashMap<>();


        ifExpression = node.getChild(0);
        ifExpressions = findAllExpressions(ifExpression);

        ifExpressions.forEach(javaNode -> System.out.printf( "ifExpression: %s%n", toString(javaNode)));

        for( int i=1; i<node.getNumChildren(); i++) {
            final JavaNode childNode = node.getChild(i);
            final List<JavaNode> astExpressionList = findAllExpressions(childNode);
            branchExpressions.put( childNode, astExpressionList);
        }

        // Now check if there is any expression in the branches that is compatible
        // with the ifExpression


        reportDuplicates(ifExpressions, branchExpressions, data);

        final List<ASTExpression> astExpressionList = node.getChild(1)
                .findDescendantsOfType(ASTExpression.class);

        for (final ASTExpression astExpression : astExpressionList) {
            final String s = toString(astExpression);
            System.out.printf( "Image: %s%n", s);
        }

        // Check if there is overlap between the ifExpression and any of the other expressions


        return super.visit(node, data);
    }

    private List<JavaNode> findAllExpressions(JavaNode node) {
        final List<JavaNode> astExpressionList = node
                .findDescendantsOfType(ASTExpression.class);

        final List<ASTRelationalExpression> astRelationalExpressions =
                node.findDescendantsOfType(ASTRelationalExpression.class);

        for( ASTRelationalExpression astRelationalExpression : astRelationalExpressions) {
            astExpressionList.addAll(findAllExpressions(astRelationalExpression.getChild(0)));
            astExpressionList.addAll(findAllExpressions(astRelationalExpression.getChild(1)));
        }

        if(isExpression(node)) {
            astExpressionList.add( node);
        } else if( node instanceof ASTPrimaryExpression) {
            if( node.getNumChildren() == 2
                    && node.getChild(0) instanceof ASTPrimaryPrefix
                    && node.getChild(1) instanceof ASTPrimarySuffix) {
                astExpressionList.add(node);
                astExpressionList.addAll(findAllExpressions(node.getChild(1)));
            }

        }
        return astExpressionList;
    }

    private boolean isExpression(Node node) {


        return node instanceof ASTExpression
                || node instanceof ASTAdditiveExpression;
    }

    private void reportDuplicates(
            List<JavaNode> ifExpressions,
            Map<Node, List<JavaNode>> branchExpressions, Object data) {
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

    private String toString(JavaNode astExpression) {
        StringBuilder stringBuilder = new StringBuilder();

        toString(stringBuilder, astExpression);

        return stringBuilder.toString();
    }

    private void toString(StringBuilder stringBuilder, JavaNode javaNode) {

        if( javaNode instanceof ASTRelationalExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            final String image = javaNode.getImage();
            stringBuilder.append(image);
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTAdditiveExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            final String image = javaNode.getImage();
            stringBuilder.append(image);
            toString(stringBuilder, javaNode.getChild(1));
        } else if( javaNode instanceof ASTArgumentList) {
            stringBuilder.append("(");
            for (JavaNode child : javaNode.children()) {
                toString(stringBuilder, child);
            }
            stringBuilder.append(")");
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

    private void addViolation(final JavaNode node, final Object data, final List<JavaNode> javaNodes,
            final String expressionString) {
        final String lines = StringUtils
                .join(javaNodes.stream().map(this::lineAndColumn).collect(Collectors.toList()), ",");
        addViolation(data, node, new String[] {expressionString,
                lines });
    }

    private String lineAndColumn(JavaNode javaNode) {
        return javaNode.getBeginLine() + ":" + javaNode.getBeginColumn();
    }
}
