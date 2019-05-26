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
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import jfxtras.labs.util.event.MouseControlUtil;

public class EvidNode extends AnchorPane
{
    private MainScreen mainControl ;
    private final EvidNode self;
    
    @FXML private AnchorPane evidNode_rootPane;
//    @FXML private HBox hbox_hypNode ;
//    @FXML private Button btn_lockHypNode ;
//    @FXML private Tooltip tooltip_lockHypNode ;
    
    @FXML private VBox evidNode_contentVBox;
    
    @FXML private HBox evidNode_titleHBox;
    @FXML private Label evidNode_lblNodeType;         // Representa o tipo do nodo, e também é a área clicável que permite a movimentação do nodos
    @FXML private TextField evidNode_nodeNameTxtField ; 
    @FXML private Label evidNode_closeBtn;            // Botao para apagar o nodo
    
    @FXML private GridPane evidNode_statesGrid;      // tabela de estados da hipótese
    
//    //Elementos de uma "linha" do nodo de hipotese
//    @FXML private TextField hypNode_stateNameTxtField ;  //nome do estado da hiptoese
//    @FXML private TextField hypNode_stateProbTxtField ;  //probabilidade do estado da hipotese
////    @FXML private BorderPane hypNode_killStateBordPane;  //conteiner do botao de remoção de estado da hipotese
//    @FXML private Button hypNode_killStateBtn ;     //botao de remoção de estado da hipotese
           
    @FXML private BorderPane evidNode_addStateBtnBorder;
    @FXML private Button evidNode_addStateBtn ;
    
    private static final int numHypColumns = 3;
//
//    private EventHandler <MouseEvent> mLinkHandleDragDetected;
//    private EventHandler <DragEvent> mLinkHandleDragDropped;
//    private EventHandler <DragEvent> mContextLinkDragOver;
//    private EventHandler <DragEvent> mContextLinkDragDropped;
//
//    private EventHandler <DragEvent> mContextDragOver;
//    private EventHandler <DragEvent> mContextDragDropped;   
 
    
    //Cria o controlador do nodo de hipótese e o associa ao .fxml correspondente
    public EvidNode() 
    {
        FXMLLoader fxmlLoader = new FXMLLoader( getClass().getResource("/userInterface/EvidNode.fxml") );
        fxmlLoader.setRoot(this); 
        fxmlLoader.setController(this);
        self = this;
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
    }
    
    @FXML // apaga um nodo de hipotese
    private void removeEvidNode() 
    {
        AnchorPane parent  = (AnchorPane) self.getParent();
        
        parent.getChildren().remove(self);
    }

    @FXML // adiciona um novo estado ao nodo de hipotese
    private void addEvidState() 
    {
        int numRows = this.getRowCount(evidNode_statesGrid); // conta o numero de linhas no grid (tabela de estados da evid)
        int numCols = this.getColCount(evidNode_statesGrid); // conta o numero de colunas no grid (tabela de estados da hipotese)
        
        Node[] rowComponents = this.evidRowSetup(numRows); // monta uma linha "padronizada" para adicionar ao nodo de hipótese
        int lastRowIndex = rowComponents.length-1 ;       // aponta para o índice do botão na tabela, para ajustá-lo na inserção no grid
        
        this.evidNode_statesGrid.addRow(numRows, rowComponents );    // adiciona a linha "padronizada" ao grid do nodo
//        this.evidNode_statesGrid.setColumnIndex(rowComponents[0], 0);// atribui explicitamente a posição "0" referente aos itens da coluna 0
        GridPane.setColumnIndex(rowComponents[0], 0) ;
        this.evidNode_statesGrid.setHalignment(rowComponents[lastRowIndex], HPos.RIGHT); // define o ajuste de alinhamento adequado para o botão dentro de sua célula
     
        
        for (int colIndex = 0; colIndex < numCols; colIndex++)
        {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS) ; // allow column to grow
            cc.setFillWidth(true); // ask nodes to fill space for column
            // other settings as needed...
            this.evidNode_statesGrid.getColumnConstraints().add(cc);
        }        
        
        System.out.println ("Gridpane dimensions: (" + this.getRowCount(evidNode_statesGrid) + 
                            "x" + this.getColCount(evidNode_statesGrid) + ")" +
                            "\nLinhas: " + this.getRowCount(evidNode_statesGrid) +
                            "\nColunas: " + this.getColCount(evidNode_statesGrid));
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
    
    //Prepara uma nova linha para ser adicionada no nodo de evidência
    //Linhas representam os estados da Evidência, e contêm: Nome do estado, P(e/H1), P(e/H2)... P(e/Hn), botão para remover estado
    //Quantidade variável de colunas, dependendo da quantidade de estados do nodo de hipótese
    private Node[] evidRowSetup(int rowCount)
    {
        int currentState = rowCount+1; //recebe o numero atual de linhas, e adiciona 1 para a próxima a ser criada
        int numberOfHypStates = this.mainControl.hypNode.getCountHypStates();   // guarda a quantidade de estados da hipótese
        Node[] rowComponents = new Node[2+numberOfHypStates]; //2 (nome do estado e botão de exlusão) + quantidade de estados da hipótese
        
        //cria o textField do novo estado
        TextField evidStateNameTxtField = new TextField();  //
        evidStateNameTxtField.setPromptText("Estado" + currentState);        
        evidStateNameTxtField.getStyleClass().add("textField-node");
        
        evidStateNameTxtField.prefColumnCountProperty().bind(evidStateNameTxtField.textProperty().length());
        evidStateNameTxtField.setPadding(Insets.EMPTY);
        evidStateNameTxtField.setMinWidth(90);
        evidStateNameTxtField.setPrefSize(USE_PREF_SIZE, USE_PREF_SIZE);
//        evidStateNameTxtField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //adiciona o componente criado na ordem em que deverá aparecer ta tabela do nodo de evidência
        
        rowComponents[0] = evidStateNameTxtField ;        
        
        for (int i = 1 ; i<= numberOfHypStates ; i++)
        {
            //cria o textField da prob do novo estado
            TextField evidStateProbTxtField = new TextField();  //
            evidStateProbTxtField.setPromptText("P(e" + currentState + "|H" + i + ")");        
            evidStateProbTxtField.getStyleClass().add("textField-node");
            evidStateProbTxtField.setTextFormatter(this.formatNumericTextField()); //adiciona o filtro de formato numérico ao textField de probabilidades
            
            
            evidStateProbTxtField.setPadding(Insets.EMPTY);
            evidStateProbTxtField.setMinWidth(90);

//            evidStateNameTxtField.setPrefSize(USE_PREF_SIZE, USE_PREF_SIZE);
            rowComponents[i] = evidStateProbTxtField ;
        }        
        
        //cria o botão de remoção do novo estado
        Button killEvidStateBtn = new Button();  //
        killEvidStateBtn.setText("-");        
        killEvidStateBtn.getStyleClass().add("btn-kill-hyp-state");
        
        // concede ao botão um identificador único, o qual deverá ser atualizado caso estados anteriores sejam removidos
        killEvidStateBtn.setId("killHypStateBtn" + currentState);
        
        //Concede ao botão a capacidade de lidar com "clicks" recebidos
        killEvidStateBtn.addEventHandler
        (   
            MouseEvent.MOUSE_CLICKED,
            new EventHandler<MouseEvent>() 
            {
                @Override public void handle(MouseEvent e)
                {
                    removeEvidState(e); //método executado quando o botão é clickado
                }
            }
        );        
        int lastPos = rowComponents.length-1 ;         // aponta para a última coluna
        rowComponents[lastPos] = killEvidStateBtn ;  // adiciona o último componente da linha (botão de exclusão de estado)
                  
        return rowComponents;
    }
    
    @FXML   // Destaca o botão de exclusão do nodo de hipótese quando o mouse passa sobre ele
    private void closeButtonHighlightOn()
    {
        this.evidNode_closeBtn.getStyleClass().clear();
        this.evidNode_closeBtn.getStyleClass().add("hyp-title-fonts");
        this.evidNode_closeBtn.getStyleClass().add("close-button-highlight-on");
    }
    
    @FXML   // Remove o destaque do botão de exclusão do nodo de hipótese quando o mouse passa sobre ele
    private void closeButtonHighlightOff()
    {
        this.evidNode_closeBtn.getStyleClass().clear();
        this.evidNode_closeBtn.getStyleClass().add("hyp-title-fonts");
        this.evidNode_closeBtn.getStyleClass().add("close-button-highlight-off");
    }

    @FXML
    private void removeEvidState(Event event) // deve apagar uma linha inteira do nodo de hipótese após o botão referente a ela ser clicado
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

        int numChildren = this.evidNode_statesGrid.getChildren().size(); //retorna a quantidade total de nodos filhos do grid
        System.out.println( "Quantidade de filhos do grid: " + numChildren);
          
        Button btn = (Button) event.getSource();    // acessa o botão que originou o evento de exclusão do estado da hipótese
        System.out.println( "Id do botão clickado: " + btn.getId());
        
        GridPane grid = (GridPane) btn.getParent(); // acessa o grid ao qual o botão clickado pertence (tabela de estados)
        System.out.println( "Id do GridPane clickado: " + grid.getId());
        
        int numRow = this.evidNode_statesGrid.getRowIndex(btn);  // informa a linha do grid à qual o botão clickado pertence
        System.out.println( "Linha do botao clickado: " + numRow);
        
        this.deleteRow(this.evidNode_statesGrid, numRow);    // deleta do grid a linha referente ao botão clickado
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
    
    
//    
//    @FXML
//    private void lockEvidNode() // sem função no momento
//    {
//        if ( this.evidNode_nodeNameTxtField.isDisabled() )
//        {
//            this.evidNode_nodeNameTxtField.setDisable(false);
//            this.evidNode_statesGrid.setDisable(false);
//            this.hypNode_addStateBtnBorder.setDisable(false);
//        }
//        else 
//        {            
//            this.evidNode_nodeNameTxtField.setDisable(true);
//            this.evidNode_statesGrid.setDisable(true);
//            this.hypNode_addStateBtnBorder.setDisable(true);
//        }
//    }
    
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
