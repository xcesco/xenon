/**
 * 
 */
package com.abubusoft.xenon.engine;

import java.util.ArrayList;

import com.abubusoft.xenon.Argon4OpenGL;
import com.abubusoft.xenon.misc.Clock;
import com.abubusoft.xenon.core.logger.ElioLogger;

/**
 * State manager. Quando si parla di frame nell'ambito dello stateManager, si indica il tempo
 * che intercorre tra il metodo {@link #frameStart()} e {@link #frameStop()}.
 * 
 * @author Francesco Benincasa
 * 
 */
public class StateManager {

	/**
	 * riferimento ad argon
	 */
	private Argon4OpenGL argon;

	/**
	 * @param argon
	 *            the argon to set
	 */
	public void setArgon(Argon4OpenGL argon) {
		this.argon = argon;
	}

	private static final StateManager instance = new StateManager();

	public static final StateManager instance() {
		return instance;
	}

	/**
	 * elenco degli shared data modificati
	 */
	private ArrayList<SharedData> sharedData = new ArrayList<>();

	/**
	 * durata dell'ultimo frame.
	 */
	private long duration;

	private int n;

	public void frameStart() {
		//ElioLogger.info("StateManager -- frameStart");
		
		// iniziamo il disegno della scena
		argon.onDrawFrameBegin();

		// ora impostiamo nuovamente il clock
		duration = Clock.now();

		// aggiorniamo tutti gli shared data
		n = sharedData.size();
		for (int i = 0; i < n; i++) {
			sharedData.get(i).update();
		}
		
		// e alla fine puliamo la lista
		sharedData.clear();
	}

	/**
	 * <p>
	 * Viene invocato nella fase LOGIC e consente di tener traccia degli shared data che sono stati modificati./
	 * <p>
	 * @param <E>
	 * 
	 * @param value
	 */
	public <E extends SharedData> E touch(E value) {
		sharedData.add(value);
		
		return value;
	}

	public void frameStop() {
		UpdateManager.instance().isReady();
		duration = Clock.now() - duration;
		//ElioLogger.info("StateManager -- frameStop (%s ms)", duration);

		// termine del disegno della scena
		argon.onDrawFrameEnd();
	}

}
