package app;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jfxtras.labs.util.event.MouseControlUtil;
import javafx.scene.Node ;

public class MainScreen extends BorderPane
{
    @FXML public BorderPane root_pane ;
    @FXML public VBox root_vBox ;
    @FXML private ToolBar tool_bar ;
    
    @FXML public AnchorPane area_bayes ;     // Área em que os nodos são adicionados
    @FXML public Button btnCreateHypNode ;   // Botão usado para criar um nodo de hipótese
    @FXML public Button btnCreateEvidNode ;  // Botão usado para criar um nodo de evidência
    @FXML public Tab tab_fuzzy;
    @FXML public Tab tab_resultados;
    @FXML public Tab tab_erros;
    
        
    HypNode hypNode ;  //representa o nodo de Hipótese criado            
    InputHandler reader = new InputHandler();
    
    //Constrói o controlador, o associando ao arquivo .fxml correspondente
    public MainScreen ()
    {
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource("/userInterface/MainScreen.fxml") );
        fxmlLoader.setRoot(this); 
        fxmlLoader.setController(this);
        try
        {
            fxmlLoader.load();
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    public void initialize()
    {
        
    }

///////////////////////////////////////////////////////////////////////////////////////////////////    
///////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Funções dos itens dos menus ///////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
    
    @FXML
    public void exit() //MenuFile_Close
    {
        Platform.exit();
    }


///////////////////////////////////////////////////////////////////////////////////////////////////    
///////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Funções dos botões da toolbar //////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
    
    // Carrega só um arquivo de entrada Bayes
    @FXML
    public void chooseFileBayes(ActionEvent arg0) // btnBayes
    {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        // Seleciona os arquivos com a janela 
////      File file = fileChooser.showOpenDialog(WINDOW);
//        String nome = file.getName();
//        System.out.println("File nome : " + nome);
//        String absolutePath = file.getAbsolutePath();
//        System.out.println("File absolute path : " + absolutePath);
     ///////////////////////////////////////////////////////////
     
             // Seleciona os arquivos sem a janela (facilitar testes)
        File file = new File ("entradaBayes.txt");
        String name = file.getName();
        System.out.println("File nome : " + name);
        String absolutePath = file.getAbsolutePath();
        System.out.println("File absolute path : " + absolutePath);
        ///////////////////////////////////////////////////////////


        //this.reader.loadTextFile(nome);
        this.reader.loadTextFile(absolutePath);
        
        this.reader.readBayes();
        //double bayesPuro = this.reader.calculateBayesMultiHypStates();
        //System.out.println(bayesPuro);
        
         
        //System.out.println("passou passou: " + absolutePath);
        this.tab_resultados.setDisable(false);
        
    }

    // Carrega um arquivo de entrada bayes e um fuzzy
    @FXML
    public void chooseFileFuzzy(ActionEvent arg0) // btnFuzzy
    {
        this.chooseFileBayes(arg0);     
     
        //teste fuzzy
        FileChooser fileChooser2 = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("FCL files (*.fcl)", "*.fcl");
        fileChooser2.getExtensionFilters().add(extFilter2);

        
        // Seleciona arquivo fcl com a janela
//        //Show save file dialog
//        File file2 = fileChooser2.showOpenDialog(WINDOW);
        ///////////////////////////////////////////////////
        
                // Seleciona os arquivos sem a janela (facilitar testes)
        File file2 = new File ("entradaFuzzy.fcl");
        String name = file2.getName();
        System.out.println("File nome : " + name);
        String absolutePath = file2.getAbsolutePath();
        System.out.println("File absolute path : " + absolutePath);
        ///////////////////////////////////////////////////////////

        String path = file2.getAbsolutePath();
        this.reader.readFuzzy(path);
        this.reader.calcFuzzyBayes();        

    }
    
    //Cria um Nodo de Hipótese
    @FXML
    public void createHypNodeUI () // btnCreateHypNode
    {
        HypNode hypNode = new HypNode(); // cria o objeto referente ao nodo de Hipótese
        MouseControlUtil.makeDraggable(hypNode); // faz com que o nodo criado seja um objeto "arrastável" na interface        
        hypNode.setMainControl(this);    // associa esta MainScreen ao HypNode criado
        this.area_bayes.getChildren().add(hypNode);  // Adiciona o elemento visual do nodo de hipótese ao contêiner da interface gráfica
        this.btnCreateHypNode.setDisable(true); // Desabilita o botão, pois só se pode haver 1 nodo de hipótese na rede
        System.out.println("Criou hypNode");
        this.hypNode = hypNode ;
    }
    
    //Cria um Nodo de Evidência
    @FXML
    public void createEvidNodeUI () // btnCreateEvidNode
    {
        EvidNode evidNode = new EvidNode(); // cria o objeto referente ao nodo de Hipótese
        MouseControlUtil.makeDraggable(evidNode); // faz com que o nodo criado seja um objeto "arrastável" na interface        
        evidNode.setMainControl(this);    // associa esta MainScreen ao HypNode criado
        this.area_bayes.getChildren().add(evidNode);  // Adiciona o elemento visual do nodo de hipótese ao contêiner da interface gráfica
        this.btnCreateHypNode.setDisable(true); // Desabilita o botão, pois só se pode haver 1 nodo de hipótese na rede
        
        System.out.println("Criou EvidNode");
        
        if ( !this.hypNode.btn_lockHypNode.isDisabled() ) // desabilita a edição do nodo de hipótese enquanto houver nodos de evidência
        {
            this.hypNode.btn_lockHypNode.setDisable(true);
        }
    }
    
///////////////////////////////////////////////////////////////////////////////////////////////////    
///////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Funções e métodos auxiliares ///////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

    // retorna todos os nodos descendentes de um Nodo
    public ArrayList<Node> getAllNodes(Parent root) 
    {
        ArrayList<Node> nodes = new ArrayList<Node>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    // método complementar do "getAllNodes"
    private static void addAllDescendents(Parent parent, ArrayList<Node> nodes)
    {
        for (Node node : parent.getChildrenUnmodifiable() )
        {
            nodes.add(node);
            if (node instanceof Parent)
            {
                addAllDescendents((Parent)node, nodes);
            }                
        }
    }
    
    public void changeStyleTab (String erro)
    {
        this.tab_erros.setText(erro);
        String styleErro = "-fx-background-color:  #ff0000;-fx-text-fill: #000000 ;-fx-font-weight: bold;";
        this.tab_erros.setStyle(styleErro);
    }
    
    public void clearStyleTab ()
    {
        this.tab_erros.setText(null);
        //String styleClear = "-fx-background-color:  #ff0000;-fx-text-fill: #000000 ;-fx-font-weight: bold;";
        this.tab_erros.setStyle(null);
    }


}
