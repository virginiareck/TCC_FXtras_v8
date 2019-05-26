package app;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import jfxtras.labs.util.event.MouseControlUtil;

public class HypNode extends AnchorPane
{
    private MainScreen mainControl ;
    private final HypNode self;
    
    @FXML public AnchorPane hypNode_rootPane;
    @FXML public HBox hbox_hypNode ;
    @FXML public Button btn_lockHypNode ;
    @FXML public Tooltip tooltip_lockHypNode ;
    
    @FXML public VBox hypNode_contentVBox;
    
    @FXML public HBox hypNode_titleHBox;
    @FXML public Label hypNode_lblNodeType;         // Representa o tipo do nodo, e também é a área clicável que permite a movimentação do nodos
    @FXML public TextField hypNode_nodeNameTxtField ; 
    @FXML public Label hypNode_closeBtn;            // Botao para apagar o nodo
    
    @FXML public GridPane hypNode_statesGrid;      // tabela de estados da hipótese
    
    //Elementos de uma "linha" do nodo de hipotese
//    @FXML public TextField hypNode_stateNameTxtField ;  //nome do estado da hiptoese
//    @FXML public TextField hypNode_stateProbTxtField ;  //probabilidade do estado da hipotese
////    @FXML private BorderPane hypNode_killStateBordPane;  //conteiner do botao de remoção de estado da hipotese
//    @FXML public Button hypNode_killStateBtn ;     //botao de remoção de estado da hipotese
           
    @FXML public BorderPane hypNode_addStateBtnBorder;
    @FXML public Button hypNode_addStateBtn ;
    
    private static final int numHypColumns = 3; // a quantidade de colunas do nodo de hipótese sempre será 3: (Nome do estado / Prob do estado / Botão de remoção)
//    public int quantHypStates ; // guarda a quantidade de estados já adicionados à hipótese
//
//    private EventHandler <MouseEvent> mLinkHandleDragDetected;
//    private EventHandler <DragEvent> mLinkHandleDragDropped;
//    private EventHandler <DragEvent> mContextLinkDragOver;
//    private EventHandler <DragEvent> mContextLinkDragDropped;
//
//    private EventHandler <DragEvent> mContextDragOver;
//    private EventHandler <DragEvent> mContextDragDropped;    
    
    //Cria o controlador do nodo de hipótese e o associa ao .fxml correspondente
    public HypNode() 
    {
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource("/userInterface/HypNode.fxml") );
        fxmlLoader.setRoot(this); 
        fxmlLoader.setController(this);
        self = this;
        self.setId("HypNodeUI");
        try
        { 
            fxmlLoader.load();
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
        //provide a universally unique identifier for this object
        setId(UUID.randomUUID().toString());
    }

    @FXML
    private void initialize()
    {
       
    }
    
    public void setMainControl (MainScreen control)
    {
        this.mainControl = control ;
        this.mainControl.setId("MainScreenUI");
    }
    
    @FXML // apaga um nodo de hipotese
    private void removeHypNode() 
    {
        AnchorPane parent  = (AnchorPane) self.getParent();
        this.mainControl.btnCreateHypNode.setDisable(false);
        this.mainControl.btnCreateEvidNode.setDisable(true);
        parent.getChildren().remove(self);
    }

    @FXML // adiciona um novo estado ao nodo de hipotese
    private void addHypState() 
    {
        int numRows = this.getRowCount(hypNode_statesGrid); // conta o numero de linhas no grid (tabela de estados da hipotese)
        int numCols = this.getColCount(hypNode_statesGrid); // conta o numero de colunas no grid (tabela de estados da hipotese)
        
        Node[] rowComponents = this.hypRowSetup(numRows); // monta uma linha "padronizada" para adicionar ao nodo de hipótese
        int lastRowIndex = rowComponents.length-1 ;       // aponta para o índice do botão na tabela, para ajustá-lo na inserção no grid
        
        this.hypNode_statesGrid.addRow(numRows, rowComponents );    // adiciona a linha "padronizada" ao grid do nodo
        this.hypNode_statesGrid.setColumnIndex(rowComponents[0], 0);// atribui explicitamente a posição "0" referente aos itens da coluna 0
//        this.hypNode_statesGrid.setHalignment(rowComponents[0], HPos.LEFT); // define o ajuste de alinhamento adequado para o botão dentro de sua célula
        this.hypNode_statesGrid.setHalignment(rowComponents[lastRowIndex], HPos.RIGHT); // define o ajuste de alinhamento adequado para o botão dentro de sua célula
        
        System.out.println ("Gridpane dimensions: (" + this.getRowCount(hypNode_statesGrid) + 
                            "x" + this.getColCount(hypNode_statesGrid) + ")" +
                            "\nLinhas: " + this.getRowCount(hypNode_statesGrid) +
                            "\nColunas: " + this.getColCount(hypNode_statesGrid));
    }
    
    //conta as linhas de um gridpane
    private int getRowCount(GridPane pane)
    {
        int numRows = pane.getRowConstraints().size();
        for (int i = 0; i < pane.getChildren().size(); i++) {
            Node child = pane.getChildren().get(i);
            if (child.isManaged()) {
                Integer rowIndex = GridPane.getRowIndex(child);
                if(rowIndex != null){
                    numRows = Math.max(numRows,rowIndex+1);
                }
            }
        }
        return numRows;
    }

    //conta as colunas de um gridpane
    private int getColCount(GridPane pane)
    {
        int numCols = pane.getColumnConstraints().size();
        for (int i = 0; i < pane.getChildren().size(); i++)
        {
            Node child = pane.getChildren().get(i);
            if (child.isManaged())
            {
                Integer colIndex = GridPane.getColumnIndex(child);
                if(colIndex != null)
                {
                    numCols = Math.max(numCols,colIndex+1);
                }
            }
        }
        return numCols;
    }
    
    //supostamente deveria pegar um nodo do grid a partir do índice, mas não parece funcionar
    public Node getNodeByRowColumnIndex (final int row, final int column, GridPane gridPane)
    {
        Node result = null;
        ObservableList<Node> children = gridPane.getChildren();

        for (Node node : children)
        {
            if(gridPane.getRowIndex(node) == row && gridPane.getColumnIndex(node) == column)
            {
                result = node;
                break;
            }
        }

    return result;
}
    
    //Prepara uma nova linha para ser adicionada no nodo de hipotese
    //Linhas representam os estados da hipótese, e contêm: Nome do estado, P(Hi), botão para remover estado
    private Node[] hypRowSetup(int rowCount)
    {
        int currentState = rowCount+1; //recebe o numero atual de linhas, e adiciona 1 para a próxima a ser criada
        Node[] rowComponents = new Node[this.numHypColumns]; //é 3 por padrão, devido ao número de colunas do nodo de hipótese
        
        //cria o textField do novo estado
        TextField hypStateNameTxtField = new TextField();  //
        hypStateNameTxtField.setPromptText("Estado" + currentState);        
        hypStateNameTxtField.getStyleClass().add("textField-node");
        hypStateNameTxtField.prefColumnCountProperty().bind(hypStateNameTxtField.textProperty().length());        
        
        //cria o textField da prob do novo estado
        TextField hypStateProbTxtField = new TextField();  //
        hypStateProbTxtField.setPromptText("P(H" + currentState + ")");        
        hypStateProbTxtField.getStyleClass().add("textField-node");
        hypStateProbTxtField.setTextFormatter(this.formatNumericTextField()); //adiciona o filtro de formato numérico ao textField de probabilidades
        
        //cria o botão de remoção do novo estado
        Button killHypStateBtn = new Button();  //
        killHypStateBtn.setText("-");        
        killHypStateBtn.getStyleClass().add("btn-kill-hyp-state");
        
        // concede ao botão um identificador único, o qual deverá ser atualizado caso estados anteriores sejam removidos
        killHypStateBtn.setId("killHypStateBtn" + currentState);
        
        //Concede ao botão a capacidade de lidar com "clicks" recebidos
        killHypStateBtn.addEventHandler
        (   
            MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() 
            {
                @Override public void handle(MouseEvent e)
                {
                    removeHypState(e); //método executado quando o botão é clickado
                }
            }
        );
        
        //adiciona os componentes criados na ordem em que deverão aparecer ta tabela do nodo de hipótese
        rowComponents[0] = hypStateNameTxtField ;
        rowComponents[1] = hypStateProbTxtField ;
        rowComponents[2] = killHypStateBtn ;  
                  
        return rowComponents;
    }
    
    @FXML   // Destaca o botão de exclusão do nodo de hipótese quando o mouse passa sobre ele
    private void closeButtonHighlightOn()
    {
        this.hypNode_closeBtn.getStyleClass().clear();
        this.hypNode_closeBtn.getStyleClass().add("hyp-title-fonts");
        this.hypNode_closeBtn.getStyleClass().add("close-button-highlight-on");
    }
    
    @FXML   // Remove o destaque do botão de exclusão do nodo de hipótese quando o mouse passa sobre ele
    private void closeButtonHighlightOff()
    {
        this.hypNode_closeBtn.getStyleClass().clear();
        this.hypNode_closeBtn.getStyleClass().add("hyp-title-fonts");
        this.hypNode_closeBtn.getStyleClass().add("close-button-highlight-off");
    }

    @FXML
    private void removeHypState(Event event) // deve apagar uma linha inteira do nodo de hipótese após o botão referente a ela ser clicado
    {        
//        int numRow = event.getsothis.hypNode_statesGrid.getRowIndex(this.hypNode_killStateBtn);        
//        System.out.println ("Linha a ser removida: " + numRow );

//        Node target = (Node) event.getTarget();
//            // traverse towards root until hypNode_statesGrid is the parent node
//            if (target != hypNode_statesGrid)
//            {
//                Node parent;
//                while ((parent = target.getParent()) != hypNode_statesGrid)
//                {
//                    target = parent;
//                }
//            }
////      
//        int numRow = this.hypNode_statesGrid.getRowIndex(target);
//        int numCol = this.hypNode_statesGrid.getColumnIndex(target);
//        
//        System.out.println( "Linha do botão clickado: " + numRow);
//        System.out.println( "Coluna do botão clickado: " + numCol);

        int numChildren = this.hypNode_statesGrid.getChildren().size(); //retorna a quantidade total de nodos filhos do grid
        System.out.println( "Quantidade de filhos do grid: " + numChildren);
          
        Button btn = (Button) event.getSource();    // acessa o botão que originou o evento de exclusão do estado da hipótese
        System.out.println( "Id do botão clickado: " + btn.getId());
        
        GridPane grid = (GridPane) btn.getParent(); // acessa o grid ao qual o botão clickado pertence (tabela de estados)
        System.out.println( "Id do GridPane clickado: " + grid.getId());
        
        int numRow = this.hypNode_statesGrid.getRowIndex(btn);  // informa a linha do grid à qual o botão clickado pertence
        System.out.println( "Linha do botao clickado: " + numRow);
        
        this.deleteRow(this.hypNode_statesGrid, numRow);    // deleta do grid a linha referente ao botão clickado
        System.out.println( "Linha apagada: " + numRow);
        
        //this.updatePromptTexts(this.hypNode_statesGrid) // atualiza os textos "default" dos campos de texto após uma linha ser apagada
    }
    
    static void deleteRow(GridPane grid, final int row) //  Apaga uma linha específica de um determinado gridPane
    {
        Set<Node> deleteNodes = new HashSet<>();    // reune os nodos que serão excluídos
        for (Node child : grid.getChildren())       // percorre todos os nodos do grid
        {            
            Integer rowIndex = GridPane.getRowIndex(child);     // get row index from current child
            
            int r = rowIndex == null ? 0 : rowIndex; // handle null values for index = 0

            if (r > row)
            {                
                GridPane.setRowIndex(child, r-1);   // decrement rows for rows after the deleted row
            } 
            else if (r == row)
            {                
                deleteNodes.add(child);             // collect matching rows for deletion
            }
        }        
        grid.getChildren().removeAll(deleteNodes);  // remove nodes from row
        
//        // Agora percorre novamente o grid, para atualizar os promptTexts dos textFields (erro ConcurrentModificationException)
//        for (Node child : grid.getChildren())       // percorre todos os nodos do grid após a exclusão
//        {            
//            Integer rowIndex = GridPane.getRowIndex(child);     // get row index from current child
//            Integer colIndex = GridPane.getColumnIndex(child) ; // get column index from current child
//            
//            int r = rowIndex == null ? 0 : rowIndex;    // handle null values for index = 0
//            int col = colIndex == null ? 0 : colIndex;  // handle null values for index = 0
//
//            if ( col == 0 && (child instanceof TextField)) // verifica se "child" é um texField referente a nome de estado
//            {
//                int textRow = r+1 ;
//                TextField child2 = (TextField) child ;      // cria uma copia do textField atual
//                child2.setPromptText("Estado" + textRow);   // atualiza o promptText dele
//                grid.getChildren().remove(child);           // remove o textField desatualizado
//                grid.add(child2, col, r) ;                  // insere o novo textField alterado no lugar do atual
//            }
//        }
    }
    
    static void updatePromptTexts(GridPane grid) // INUTIL POR AGORA
    {
//        Set<Node> deleteNodes = new HashSet<>();
        for (Node child : grid.getChildren())
        {
            // get index from child
            Integer rowIndex = GridPane.getRowIndex(child);

            // handle null values for index=0
            int r = rowIndex == null ? 0 : rowIndex;

//            if (r > row)
//            {
//                // decrement rows for rows after the deleted row
//                GridPane.setRowIndex(child, r-1);
//            } 
//            else if (r == row)
//            {
//                // collect matching rows for deletion
//                deleteNodes.add(child);
//            }
        }
//        // remove nodes from row
//        grid.getChildren().removeAll(deleteNodes);
    }
    
    @FXML
    private void lockHypNode()
    {
        if ( this.hypNode_nodeNameTxtField.isDisabled() )
        {
            self.mainControl.btnCreateEvidNode.setDisable(true); // desabilita a criação de evidências
            this.hypNode_nodeNameTxtField.setDisable(false);
            this.hypNode_statesGrid.setDisable(false);
            this.hypNode_addStateBtnBorder.setDisable(false);
        }
        else 
        {            
            this.hypNode_nodeNameTxtField.setDisable(true);
            this.hypNode_statesGrid.setDisable(true);
            this.hypNode_addStateBtnBorder.setDisable(true);
            
            if ( this.isHypNodeComplete() )
            {
//                this.quantHypStates = this.getRowCount(this.hypNode_statesGrid) ; // define a quantidade de estados contidos na hipótese
                self.mainControl.btnCreateEvidNode.setDisable(false); //habilita a criação de evidências
            }
        }
    }
    
    public int getCountHypStates ()
    {
        return this.getRowCount(this.hypNode_statesGrid) ; // conta as linhas do grid - cada linha é 1 estado
    }
    
    // verifica se todos os campos do nodo de hipótese foram preenchidos
    // apenas permitirá a criação de nodos de evidência caso o retorno seja "true"
    private boolean isHypNodeComplete()
    {
        if ( this.getRowCount(this.hypNode_statesGrid) < 1 ) // verifica se pelo menos 1 estado foi adicionado
        {
            String erro = "ERRO: Nenhum estado adicionado à hipótese! O nodo de hipótese deve possuir ao menos 1 estado!";
            System.out.println (erro);
            this.mainControl.changeStyleTab(erro);
            return false;
        }
        else
        {
            this.mainControl.clearStyleTab();
        }
        
        for (Node child : self.mainControl.getAllNodes(this)) // percorre todos os nodos do grid após a exclusão
        {
            System.out.println ( "\n >>> ID do nodo atual: " + child.getId() + " / StyleType:" + child.getStyleClass());
            if ( child instanceof TextField ) // verifica se "child" é um texField referente a nome de estado
            {
                TextField child2 = (TextField) child ;      // cria uma copia do textField atual
                System.out.println ( "Texto lido no textField: " + child2.getText() );
                if ( child2.getText().isEmpty() )
                {                    
                    String erro = "ERRO: 1 ou mais campos de texto não preenchido(s) no nodo de hipótese! Todos os campos devem ser preenchidos antes de continuar!";
                    System.out.println (erro);
                    this.mainControl.changeStyleTab(erro);                    
                    return false ;
                }                  
            }
        }
        return true ;
    }
    
    // Cria um formatador de texto para garantir apenas inputs numéricos nos textFields de probabilidades
    public TextFormatter formatNumericTextField ()
    {
        Pattern pattern = Pattern.compile("\\d*|\\d+\\.\\d*"); // filtro para permitir apenas inputs numéricos separados por até 1 único "."       
        TextFormatter formatter = new TextFormatter
        (
            ( UnaryOperator<TextFormatter.Change> ) change -> 
            {
                return pattern.matcher(change.getControlNewText()).matches() ? change : null;
            }
        );
        
        return formatter ;
    }
}
