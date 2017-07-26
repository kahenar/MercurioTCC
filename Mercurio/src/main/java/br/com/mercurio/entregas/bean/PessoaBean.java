package br.com.mercurio.entregas.bean;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import org.omnifaces.util.Messages;
import br.com.mercurio.entregas.dao.PessoaDAO;
import br.com.mercurio.entrega.domain.CEP;
import br.com.mercurio.entrega.domain.Pessoa;
import com.google.gson.Gson;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;



@SuppressWarnings("serial")
@ManagedBean
@ViewScoped

public class PessoaBean implements Serializable {

	private Pessoa pessoa;
	private List<Pessoa> pessoas;


	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public void setPessoas(List<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}

	public void buscarCEP() {
		try {
			Client cliente = ClientBuilder.newClient();
			WebTarget caminho = cliente
					.target("http://viacep.com.br/ws/" + pessoa.getCep().replace(".", "").replace("-", "") + "/json/");
			String json = caminho.request().get(String.class);

			Gson gson = new Gson();
			CEP cep = gson.fromJson(json, CEP.class);

			if (cep.getErro() == null) {
				pessoa.setBairro(cep.getBairro());
				pessoa.setRua(cep.getLogradouro());
				pessoa.setUf(cep.getUf());
			} else {
				Messages.addGlobalError("CEP invÃ¡lido");
			}
		} catch (RuntimeException erro) {
			Messages.addGlobalError("Ocorreu um erro ao tentar obter os dados do CEP");
			erro.printStackTrace();
		}
	}
	
	
	

	@PostConstruct
	public void listar() {
		try {
			PessoaDAO pessoaDAO = new PessoaDAO();
			pessoas = pessoaDAO.listar("nome");
		} catch (RuntimeException erro) {
			Messages.addGlobalError("Ocorreu um erro ao tentar listar as pessoas");
			erro.printStackTrace();
		}
	}

	public void novo() {
		try {
			pessoa = new Pessoa();
		} catch (RuntimeException erro) {
			Messages.addGlobalError("Ocorreu um erro ao tentar gerar uma nova pessoa");
			erro.printStackTrace();
		}

	}

	public void editar(ActionEvent evento) {
		try {
			pessoa = (Pessoa) evento.getComponent().getAttributes().get("pessoaSelecionada");
		} catch (RuntimeException erro) {
			Messages.addGlobalError("Ocorreu um erro ao tentar selecionar uma pessoa");
		}
	}

	public void salvar() {
		try {
			PessoaDAO pessoaDAO = new PessoaDAO();
			pessoaDAO.merge(pessoa);
			pessoas = pessoaDAO.listar("nome");
			pessoa = new Pessoa();
		} catch (RuntimeException erro) {
			Messages.addGlobalError("Ocorreu um erro ao tentar salvar a pessoa");
			erro.printStackTrace();
		}
	}

	public void excluir(ActionEvent evento) {
		try {
			pessoa = (Pessoa) evento.getComponent().getAttributes().get("pessoaSelecionada");
			PessoaDAO pessoaDAO = new PessoaDAO();
			pessoaDAO.excluir(pessoa);
			pessoas = pessoaDAO.listar();
			Messages.addGlobalInfo("Pessoa removido com sucesso");
		} catch (RuntimeException erro) {
			Messages.addFlashGlobalError("Ocorreu um erro ao tentar remover o pessoa");
			erro.printStackTrace();

		}

	}

	

}