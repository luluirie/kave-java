package cc.kave.commons.model.names.csharp;

import java.util.Map;

import com.google.common.collect.MapMaker;

import cc.kave.commons.model.names.BundleName;
import cc.kave.commons.model.names.BundleVersion;

public class CsAssemblyName extends CsName implements BundleName {
	private static final Map<String, CsAssemblyName> nameRegistry = new MapMaker()
			.weakValues().makeMap();

	public static BundleName newAssemblyName(String identifier) {
		if (!nameRegistry.containsKey(identifier)) {
			nameRegistry.put(identifier, new CsAssemblyName(identifier));
		}
		return nameRegistry.get(identifier);
	}

	private CsAssemblyName(String identifier) {
		super(identifier);
	}

	@Override
	public BundleVersion getVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}
}
