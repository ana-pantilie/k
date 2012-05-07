package ro.uaic.info.fmse.k;

import org.w3c.dom.Element;

import ro.uaic.info.fmse.loader.Constants;
import ro.uaic.info.fmse.parsing.Visitor;

public class Constant extends Term {
	String sort;
	String value;

	public Constant(Element element) {
		super(element);
		this.sort = element.getAttribute(Constants.SORT_sort_ATTR);
		this.value = element.getAttribute(Constants.VALUE_value_ATTR);
	}

	public String toString() {
		return value + " ";
	}

	@Override
	public String toMaude() {
		return value;
	}

	public String getSort() {
		return sort;
	}

	public String getValue() {
		return value;
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
