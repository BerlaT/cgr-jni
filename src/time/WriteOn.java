/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package time;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe creata per stampare su file il tempo di simulazione calcolato sul
 * metodo cgrForward nel package routing obiettivo: la stima in modo
 * approssimativo del tempo di elaborazione
 * 
 * Esempio di utilizzo:
 * 
 * public int cgrForward(Message m, DTNHost terminusNode) { 
 * 		long t1, t2;
 * 
 * 		t1 = System.currentTimeMillis(); 
 * 		int res = Libcgr.cgrForward(this.getHost().getAddress(), m, terminusNode.getAddress());
 * 		t2 = System.currentTimeMillis();
 *
 * 		new WriteOn().write(t1, t2, m.getId(), m.getFrom().getAddress(),
 * 		m.getTo().getAddress());
 * 
 * 		return res; 
 * }
 *
 * Per inizializzare il file di testo risultate, ovvero mettere la riga di
 * intestazione:  "TI ; T2 ; ID ; FROM ; TO"
 *
 * bisogna inserire all'inizio della classe core.DTNSim (dentro il main, circa
 * dopo la riga 43), la seguente riga:
 *
 * new WriteOn().init();
 * 
 * @author giuseppe
 */
public class WriteOn {

	private String nomeFile = "TempoDiEsecuzione.txt";

	/**
	 * @param t1 = tempo inizio
	 * @param t2 = tempo fine
	 * @param id = identificativo messaggio
	 * @param from = indirizzo mittente
	 * @param to = inditizzo destinatario
	 */
	public void write(long t1, long t2, String id, int from, int to) {
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(nomeFile, true));

			String toFile = t1 + " ; " + t2 + " ;  " + id + "  ;   " + from + "  ; " + to + System.lineSeparator();
			writer.write(toFile);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			System.err.printf("Error in file");
			e.printStackTrace();
		}
	}

	public void init() {
		File file = new File(nomeFile);

		try {
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(nomeFile));

			String toFile = "      TI      ;      T2       ;  ID  ; FROM ; TO " + System.lineSeparator();
			writer.write(toFile);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			System.err.printf("Error in file");
			e.printStackTrace();
		}

	}

}
