package com.abubusoft.xenon;

import java.io.Serializable;

import com.abubusoft.kripton.android.Logger;
import com.abubusoft.xenon.settings.ArgonSettings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manager di base per le varie configurazioni.
 * 
 * @author Francesco Benincasa
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class Argon4BaseImpl<E extends ArgonApplication> implements Argon, Serializable {

	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * contesto android
	 */
	protected Context context;

	/**
	 * preference salvate
	 */
	public SharedPreferences preferences;

	/**
	 * applicazione da eseguire
	 */
	public E application;

	protected Argon4BaseImpl() {
		// non abbiamo ancora eseguito una volta l'applicazione
		// serve per capire cosa fare su onCreate dell'activity e dei service
		firstTime = true;

	}

	private static final long serialVersionUID = -299952463307885714L;

	/**
	 * indica se l'applicazione è stata caricata almeno una volta. (Serve sull'on startup degli action)
	 */
	protected boolean firstTime;

	/**
	 * configurazione della libreria
	 */
	public ArgonSettings settings;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abubusoft.xenon.Argon#onStartup(android.content.Context, com.abubusoft.xenon.settings.ArgonSettings, android.content.SharedPreferences)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onStartup(Context contextValue, ArgonSettings settingsValue, SharedPreferences preferenceValue) {
		// 1 - impostiamo il context e le preference
		// (http://android-developers.blogspot.de/2009/01/avoiding-memory-leaks.html)
		context = contextValue;
		preferences = preferenceValue;
		settings = settingsValue;

		// 2 - instanziamo config
		try {
			// impostiamo la configurazione una config di default
//			if (settings.application.configClazz == null) {
//				Logger.warn("No configClass was specifed, we use the default class %s", ArgonConfig.class);
//				//settings.application.configClazz = ArgonConfig.class.getName();
//			}

			String configClazz=settings.application.configClazz;
			
			// se ci sono dipendenze con il pack manager lo istanziamo
//			if (ConfigBase.hashPackManagerDependencies(configClazz)) {
//				PackManager.instance().init(context);
//			}
//			// carichiamo la configurazione di default
//			ConfigBase config = (ConfigBase) Class.forName(configClazz).newInstance();
//			config.readPreferences(contextValue);

//			ArgonBeanContext.setBean(ArgonBeanType.CONFIG, config);
		} catch (Exception e) {
			Logger.fatal(e.getMessage());
			e.printStackTrace();
			throw (new RuntimeException(e));
		}

		// 2 - creiamo application (recuperato da settings) e valorizza eventuali Inject
		try {
			application = (E) ArgonBeanContext.createInstance(Class.forName(settings.application.clazz.trim()));
		} catch (Exception e) {
			e.printStackTrace();
			throw (new RuntimeException(e));
		}

		// 3 - imposta le injection
		ArgonBeanContext.setBean(ArgonBeanType.APPLICATION, application);

		// 4 - impostiamo il context ed avvia l'argon application
		application.setArgon(this);
		application.onStartup();

	}
}
