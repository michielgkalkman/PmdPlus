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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DupIfRule extends AbstractJavaRule {
    @Override
    public Object visit(ASTIfStatement node, Object data) {

        // Determine the expression in the if statement.
        ASTExpression ifExpression = null;


        for( final Node childNode : node.children() ) {
            if( childNode instanceof ASTExpression) {
                ifExpression = (ASTExpression) childNode;
                break;
            }
        }
        
        final List<ASTExpression> astExpressionList = node
                .findDescendantsOfType(ASTExpression.class);



        for (final ASTExpression astExpression : astExpressionList) {
            StringBuilder stringBuilder = new StringBuilder();

            toString(stringBuilder, astExpression);

            System.out.printf( "Image: %s%n", stringBuilder);
        }

        return super.visit(node, data);
    }

    private void toString(StringBuilder stringBuilder, JavaNode javaNode) {

        if( javaNode instanceof ASTRelationalExpression) {
            toString(stringBuilder, javaNode.getChild(0));
            final String image = javaNode.getImage();
            stringBuilder.append(image);
            toString(stringBuilder, javaNode.getChild(1));
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

    @Override
	public Object visit(final ASTMethodDeclaration node, final Object data) {
		try {

			final Map<String, List<ASTName>> image2LinesNummbers = new HashMap<>();

			final List<ASTName> astNames = node
					.findDescendantsOfType(ASTName.class);

			for (final ASTName astName : astNames) {
				final String image = astName.getImage();

				final Node parent = astName.getNthParent(1);
				final Node grandParent = astName.getNthParent(2);

				if (!(parent instanceof ASTNameList)
						&& !(grandParent instanceof ASTAnnotation)
						&& !(parent instanceof ASTMarkerAnnotation)) {

					final ASTPrimaryExpression astPrimaryExpression = (ASTPrimaryExpression) astName
							.getNthParent(2);

					List<Node> arguments = astPrimaryExpression
							.findChildNodesWithXPath("PrimarySuffix/Arguments");

					if (!arguments.isEmpty()) {
						final ASTArguments astArguments = (ASTArguments) arguments
								.get(0);

						if (astArguments.size() == 0) {
                            List<ASTName> list = image2LinesNummbers.computeIfAbsent(image,
                                    k -> new ArrayList<>());

                            list.add(astName);
						}
					}
				}
			}

			processImages(node, data, image2LinesNummbers);
		} catch (final JaxenException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (final ClassCastException e1) {
			final String x = ((RuleContext) data).getSourceCodeFilename() + ":"
					+ node.getBeginLine();

			throw new RuntimeException(x, e1);
		}

		return super.visit(node, data);
	}

    private void processImages(final ASTMethodDeclaration node, final Object data,
            final Map<String, List<ASTName>> image2LinesNummbers) {
        for (final Entry<String, List<ASTName>> entry : image2LinesNummbers
        		.entrySet()) {
        	final List<ASTName> astNames = entry.getValue();
        	if (astNames.size() > 1) {
                final String image = entry.getKey();

                // Check if image is actually a void method:
                
        	    final ASTName astName = astNames.get(0);


                final JavaTypeDefinition typeDefinition = astName.getTypeDefinition();
                if (typeDefinition != null && typeDefinition.isExactType()) {
                    final String[] split = StringUtils.split( image, '.');
                    
                    Class<?> type = typeDefinition.getType();
                    
                    boolean fVoid = false;
                    
                    for( int s=1; s<split.length; s++) {
                        try {
                            Field field = null;
                            final Field[] fields = type.getFields();
                            for (Field value : fields) {
                                if (split[s].equals(value.getName())) {
                                    field = value;
                                    break;
                                }
                            }
                            
                            if( field != null) {
                                type = field.getType();
                            } else {
                                Method method = null;
                                final Method[] methods = type.getMethods();

                                for (Method value : methods) {
                                    if (split[s].equals(value.getName())) {
                                        method = value;
                                        break;
                                    }
                                }
                                
                                if( method != null) {
                                    if( "void".equals(method.getReturnType().getTypeName())) {
                                        fVoid = true;
                                    }
                                }
                            }
                        } catch (SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    
                    if( !fVoid) {
                        addViolation(node, data, astNames, image);
                    }
                } else {
                    final NameDeclaration nameDeclaration = astName.getNameDeclaration();
                    if (nameDeclaration != null) {
                        try {
                        if( !nameDeclaration.getNode().getFirstParentOfType(ASTMethodDeclaration.class).findChildrenOfType(ASTResultType.class).get(0).isVoid()) {
                            addViolation(node, data, astNames, image);
                        }
                        } catch( Throwable throwable) {
                            System.err.println( astName.getBeginLine());
                        }
                    } else {
                        addViolation(node, data, astNames, image);
                    }
                }
        	}
        }
    }

    private void addViolation(final ASTMethodDeclaration node, final Object data, final List<ASTName> astNames,
            final String image) {
        final String lines = StringUtils
                .join(astNames.stream().map(AbstractNode::getBeginLine).collect(Collectors.toList()), ",");
        addViolation(data, node, new String[] {image,
                lines });
    }

}
