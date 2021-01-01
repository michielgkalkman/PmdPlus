package org.taHjaj.wo.pmdplus.dup;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.*;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.typeresolution.typedefinition.JavaTypeDefinition;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;
import org.jaxen.expr.RelationalExpr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DupIfRule extends AbstractJavaRule {
    @Override
    public Object visit(ASTIfStatement node, Object data) {

        // Determine the expression in the if statement.
        // Determine the expressions in the branches
        ASTExpression ifExpression = null;
        Map<Node, List<ASTExpression>> ifExpressions = new HashMap<>();
        Map<Node, List<ASTExpression>> branchExpressions = new HashMap<>();

        for( final Node childNode : node.children() ) {
            if( childNode instanceof ASTExpression) {
                ifExpression = (ASTExpression) childNode;
                final List<ASTExpression> astExpressionList = childNode
                        .findDescendantsOfType(ASTExpression.class);
                astExpressionList.add(ifExpression);
                ifExpressions.put(childNode, astExpressionList);
            } else {
                final List<ASTExpression> astExpressionList = childNode
                        .findDescendantsOfType(ASTExpression.class);
                branchExpressions.put( childNode, astExpressionList);
            }
        }

        // Now check if there is any expression in the branches that is compatible
        // with the ifExpression

        boolean fFound = false;


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

    private void reportDuplicates(
            Map<Node, List<ASTExpression>> ifExpressions,
            Map<Node, List<ASTExpression>> branchExpressions, Object data) {
        ifExpressions.entrySet().forEach(ifExpression -> {
            for( ASTExpression ifE : ifExpression.getValue()) {
                branchExpressions.entrySet().stream().flatMap(e -> e.getValue().stream()).forEach(statementExpression -> {
                    final String ifExpressionString = toString(ifE);
                    final String statementExpressionString = toString(statementExpression);
                    System.out.printf("Compare %s with %s%n", ifExpressionString, statementExpressionString);
                    if (ifExpressionString.equals(statementExpressionString)) {
                        System.out.printf("Equal %s with %s%n", ifExpressionString, statementExpressionString);
                        addViolation(ifE, data, Arrays.asList(ifE, statementExpression), ifE.getImage());
                    } else {
                        System.out.printf("Not equal %s with %s%n", ifExpressionString, statementExpressionString);
                    }
                });
            }
        });
    }

    private String toString(ASTExpression astExpression) {
        StringBuilder stringBuilder = new StringBuilder();

        toString(stringBuilder, astExpression);

        final String s = stringBuilder.toString();
        return s;
    }

    private void toString(StringBuilder stringBuilder, JavaNode javaNode) {

        if( javaNode instanceof ASTRelationalExpression) {
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


    private void addViolation(final ASTExpression node, final Object data, final List<ASTExpression> astExpressions,
            final String image) {
        final String lines = StringUtils
                .join(astExpressions.stream().map(AbstractNode::getBeginLine).collect(Collectors.toList()), ",");
        addViolation(data, node, new String[] {image,
                lines });
    }

}
