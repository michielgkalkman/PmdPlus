package org.taHjaj.wo.pmdplus.dup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTArguments;
import net.sourceforge.pmd.lang.java.ast.ASTMarkerAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTNameList;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaTypeNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import net.sourceforge.pmd.lang.java.typeresolution.typedefinition.JavaTypeDefinition;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;

public class DupRule extends AbstractJavaRule {

	@Override
	public Object visit(final ASTMethodDeclaration node, final Object data) {
		try {

			final Map<String, List<ASTName>> image2LinesNummbers = new HashMap<String, List<ASTName>>();

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

					List arguments = astPrimaryExpression
							.findChildNodesWithXPath("PrimarySuffix/Arguments");

					if (!arguments.isEmpty()) {
						final ASTArguments astArguments = (ASTArguments) arguments
								.get(0);

						if (astArguments.getArgumentCount() == 0) {
							List<ASTName> list = image2LinesNummbers.get(image);

							if (list == null) {
								list = new ArrayList<ASTName>();
								image2LinesNummbers.put(image, list);
							}

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
                            for( int i=0; i<fields.length; i++) {
                                if(split[s].equals(fields[i].getName())) {
                                    field = fields[i];
                                    break;
                                }
                            }
                            
                            if( field != null) {
                                type = field.getType();
                            } else {
                                Method method = null;
                                final Method[] methods = type.getMethods();
                                
                                for( int j=0; j<methods.length; j++) {
                                    if( split[s].equals(methods[j].getName())) {
                                        method = methods[j];
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
                        if( !nameDeclaration.getNode().getFirstParentOfType(ASTMethodDeclaration.class).findChildrenOfType(net.sourceforge.pmd.lang.java.ast.ASTResultType.class).get(0).isVoid()) {
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
                .join(astNames.stream().map( a -> a.getBeginLine()).collect(Collectors.toList()), ",");
        addViolation(data, node, new String[] {image,
                lines });
    }

	private void dump(final AbstractJavaTypeNode node) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append(node.getClass().getSimpleName())
				.append("=================================================================\nScope:")
				.append(node.getScope()).append("\nImage:")
				.append(node.getImage()).append("\n");
		System.out.println(stringBuilder);
	}
}
