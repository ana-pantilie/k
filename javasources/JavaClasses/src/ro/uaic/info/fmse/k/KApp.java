package ro.uaic.info.fmse.k;

import org.w3c.dom.Element;

import ro.uaic.info.fmse.loader.JavaClassesFactory;
import ro.uaic.info.fmse.parsing.Visitor;
import ro.uaic.info.fmse.utils.xml.XML;

public class KApp extends Term {
	Term label;
	Term child;

	public KApp(Element element) {
		super(element);
		Element elm = XML.getChildrenElements(element).get(0);
		Element elmBody = XML.getChildrenElements(elm).get(0);
		this.label = (Term) JavaClassesFactory.getTerm(elmBody);

		elm = XML.getChildrenElements(element).get(1);
		this.child = (Term) JavaClassesFactory.getTerm(elm);
	}

	public String toString() {
		return this.label + "(" + this.child + ")";
	}

	@Override
	public String toMaude() {
		return label.toMaude() + "(" + child.toMaude() + ") ";
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
		label.accept(visitor);
		child.accept(visitor);
	}
}
