/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.lyst;

/**
 * Classe che implementa un elemento di LinkedList
 * @author michele
 *
 */
public class LystElt {
	
	private Lyst list;
	/**
	 * data e' un long perche' prende dal C direttamente il puntatore all'oggetto da mettere nella lista
	 * In pratica un elemento della lista e' un puntatore a un'area di memoria gestita dal C.
	 * Il C lo salva nella lista e lo tira fuori quando gli serve, usando funzioni di java.
	 */
	private long data;
	
	public LystElt(long data)
	{
		this.data = data;
		this.list = null;
	}

	public Lyst getList() {
		return list;
	}

	public void setList(Lyst list) {
		this.list = list;
	}

	public long getData() {
		return data;
	}

	public void setData(long data) {
		this.data = data;
	}
	
	

}