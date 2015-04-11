package com.myselia.javadaemon.extraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.mycelia.common.communication.units.Atom;
import com.mycelia.common.communication.units.Transmission;
import com.mycelia.common.constants.opcode.ComponentType;

public class DaemonComponentInfoExtract {

	private static Map<String, String> setupMapPrototype;
	private static Map<String, String> setupMap;
	private static HashSet<String> reqSet;

	static {
		reqSet = new HashSet<String>();
		reqSet.add("ip");
		reqSet.add("type");
		reqSet.add("mac");
		reqSet.add("hashID");
		prepareSetupMap(reqSet);
	}

	public static synchronized String[] createHandler(Transmission setupPacket) {
		String[] infoSet = new String[4];
		int reqCount = reqSet.size();
		System.err.println("SETUP PACKET: " + setupPacket.toString());
		Iterator<Atom> it = setupPacket.get_atoms().iterator();
		while (it.hasNext()) {
			if (reqCount == 0)
				break;
			Atom a = it.next();
			String atomField = a.get_field();
			if (reqSet.contains(atomField)) {
				setupMap.put(atomField, a.get_value());
				reqCount--;
			}
		}

		if (reqCount == 0) {
			switch (ComponentType.valueOf(setupMap.get("type"))) {

			case SANDBOXSLAVE:
				infoSet[0] = setupMap.get("ip");
				infoSet[0] = setupMap.get("type");
				infoSet[0] = setupMap.get("mac");
				infoSet[0] = setupMap.get("hash");
				break;
			default:

			}
		} else {
			System.err.println("Could not create a network component handler!");
		}

		// Reset the map for the next component that comes along
		resetSetupMap();
		return infoSet;
	}

	/**
	 * Run once at static init. Takes a set of fields that the component must
	 * send in the handshake packet to be properly initialized.
	 * 
	 * @param s
	 *            The set of required parameters.
	 */
	private static void prepareSetupMap(Set<String> s) {
		setupMapPrototype = new HashMap<String, String>();
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String requiredField = it.next();
			System.out.println("Found a required field: " + requiredField);
			setupMapPrototype.put(requiredField, "");
		}
		setupMap = new HashMap<String, String>(setupMapPrototype);
	}

	/**
	 * This method is run every time the builder finishes creating a handler. It
	 * resets the setupMap that is passed to the handler for initialization to
	 * the empty prototype, ensuring it is ready to create a new handler.
	 */
	private static void resetSetupMap() {
		setupMap = new HashMap<String, String>(setupMapPrototype);
	}

}
