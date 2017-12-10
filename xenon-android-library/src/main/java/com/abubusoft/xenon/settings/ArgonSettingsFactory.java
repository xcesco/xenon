/**
 * 
 */
package com.abubusoft.xenon.settings;

import org.abubu.elio.Uncryptable;


/**
 * <p>Factory dell'applicazione. Fornisce le informazioni di base quali ad
 * esempio la classe dell'applicazione e le altre configurazioni.</p>
 * 
 * <p>Deve essere realizzata dall'activity o dal service.</p>
 * 
 * @author Francesco Benincasa
 *
 */
public interface ArgonSettingsFactory extends Uncryptable {
	
	/**
	 * <p>Recupera le informazioni relative ai settings. Da questi verrà eventualmente
	 * recuperata anche la classe dell'applicazione.</p>
	 * 
	 * @return
	 * 		ArgonSettings
	 */
	ArgonSettings buildSettings();

}
