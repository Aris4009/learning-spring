package com.example.demo._1_12_4;

public class ClientServiceImpl implements ClientService {

	private ClientDao clientDao;

	public ClientDao getClientDao() {
		return clientDao;
	}

	public void setClientDao(ClientDao clientDao) {
		this.clientDao = clientDao;
	}
}
