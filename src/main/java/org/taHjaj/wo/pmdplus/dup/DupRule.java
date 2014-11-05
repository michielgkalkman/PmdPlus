package org.taHjaj.wo.pmdplus.dup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.lang3.StringUtils;
import org.jaxen.JaxenException;

public class DupRule extends AbstractJavaRule {

	@Override
	public Object visit(final ASTMethodDeclaration node, final Object data) {
		try {

			final Map<String, List<Integer>> image2LinesNummbers = new HashMap<String, List<Integer>>();

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

					List arguments;
					arguments = astPrimaryExpression
							.findChildNodesWithXPath("PrimarySuffix/Arguments");

					if (!arguments.isEmpty()) {
						final ASTArguments astArguments = (ASTArguments) arguments
								.get(0);

						if (astArguments.getArgumentCount() == 0) {
							final String name = image.contains(".") ? StringUtils
									.substringBefore(image, ".") : image;

							// final List variableDeclaratorId = node
							// .findChildNodesWithXPath(String
							// .format("//VariableDeclaratorId[@Image='%s']",
							// name));

							// if (!variableDeclaratorId.isEmpty()) {
							List<Integer> list = image2LinesNummbers.get(image);

							if (list == null) {
								list = new ArrayList<Integer>();
								image2LinesNummbers.put(image, list);
							}

							list.add(astName.getBeginLine());
							// }
						}
					}
				}
			}

			for (final Map.Entry<String, List<Integer>> entry : image2LinesNummbers
					.entrySet()) {
				if (entry.getValue().size() > 1) {
					final String lines = StringUtils
							.join(entry.getValue(), ",");
					addViolation(data, node, new String[] { entry.getKey(),
							lines });
				}
			}
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
