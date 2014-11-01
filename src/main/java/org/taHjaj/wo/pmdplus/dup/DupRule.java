package org.taHjaj.wo.pmdplus.dup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaTypeNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

import org.jaxen.JaxenException;


public class DupRule extends AbstractJavaRule {
	
	
//    @Override
//	public Object visit(final ASTPrimaryExpression node, final Object data) {
//    	System.out.println(node.getScope());
//		return super.visit(node, data);
//	}
//	
//    @Override
//	public Object visit(final ASTName node, final Object data) {
//    	System.out.println("Name: " + node.getScope());
//		return super.visit(node, data);
//	}
	
    @Override
	public Object visit(final ASTVariableDeclaratorId node, final Object data) {
    	dump(node);
    	
    	final String nameDeclaration = node.getNameDeclaration().getName();
    	
    	final Map<String, List<Integer>> image2LinesNummbers = new HashMap<String, List<Integer>>();
    	
    	try {
			final List findChildNodesWithXPath = node.findChildNodesWithXPath( "//Name");
			for( final Object child : findChildNodesWithXPath) {
				final ASTName childNode = (ASTName) child;
				
				final String image = childNode.getImage();
				
				if( image.startsWith(nameDeclaration) && ! image.equals( nameDeclaration)) {
					
					List<Integer> list = image2LinesNummbers.get( image);
					
					if( list == null) {
						list = new ArrayList<Integer>();
						image2LinesNummbers.put(image, list);
					}
					
					list.add( childNode.getBeginLine());
					
					
					System.out.println( ">>>" + image);
				}
			}
			
			for( final Map.Entry<String, List<Integer>> entry : image2LinesNummbers.entrySet()) {
				if( entry.getValue().size() > 1) {
		            addViolation(data, node);
				}
			}
			
		} catch (final JaxenException e) {
			// TODO Auto-generated catch block
		
			e.printStackTrace();
		}
    	
    	
    	return super.visit(node, data);
	}
	
    @Override
	public Object visit(final ASTPrimaryPrefix node, final Object data) {
    	dump(node);
		return super.visit(node, data);
	}
	
    @Override
	public Object visit(final ASTName node, final Object data) {
    	dump(node);
		return super.visit(node, data);
	}

	private void dump(final AbstractJavaTypeNode node) {
		final StringBuilder stringBuilder = new StringBuilder();
    	stringBuilder.append(node.getClass().getSimpleName())
    		.append( "=================================================================\nScope:")
    		.append( node.getScope())
    		.append( "\nImage:")
    		.append( node.getImage())
    		.append( "\n");
    	System.out.println( stringBuilder);
	}
    
//	@Override
//	public Object visit(final ASTWhileStatement node, final Object data) {
//        final Node firstStmt = node.jjtGetChild(1);
//        if (!hasBlockAsFirstChild(firstStmt)) {
//            addViolation(data, node);
//        }
//        return super.visit(node,data);
//    }
//    private boolean hasBlockAsFirstChild(final Node node) {
//        return (((AbstractNode) node).jjtGetNumChildren() != 0 && (((AbstractNode) node).jjtGetChild(0) instanceof ASTBlock));
//    }
}
    