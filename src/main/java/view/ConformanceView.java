package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.StringUtils;

import algorithm.*;
import dk.brics.automaton.RegExp;
import model.*;
import parser.ImportAutFile;
import util.ModelImageGenerator;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.TextArea;
import javax.swing.JLabel;

import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.Toolkit;
import javax.swing.border.MatteBorder;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JTextArea;

import javax.swing.SwingConstants;

public class ConformanceView extends JFrame {
	private JComboBox cbModel;
	private JComboBox cbLabel;
	private JLabel lblImplementation;
	private JLabel lblSpecification;
	private JLabel lblOutput;
	private JLabel lblInput;
	private JLabel lblLabelInp;
	private JLabel lblLabelOut;
	private JLabel lblRotulo;
	private JPanel contentPane;
	private JTextField tfImplementation;
	private JTextField tfSpecification;
	private JLabel lblD;
	private JLabel lblRegexD;
	private JLabel lblF;
	private JLabel lblRegexF;
	private JLabel lblmodelIoco;
	private JLabel lblImplementationIoco;
	private JButton btnViewModelIoco;
	private JButton btnViewImplementationIoco;
	private JButton btnViewModelLang;
	private JButton btnViewImplementationLang;
	private JLabel lblLabelIoco;
	private JLabel lblLabel;
	JLabel lblOutput_1;
	JLabel lblnput;
	TextArea lblWarningLang;
	TextArea lblWarningIoco;

	private String pathImplementation;
	private String pathSpecification;
	private String failPath;
	JFileChooser fc = new JFileChooser();
	private JTextField tfInput;
	private JTextField tfOutput;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	JLabel lblInputIoco;
	JLabel lblOutputIoco;
	JTextArea taTestCasesIoco;

	private SystemColor backgroundColor = SystemColor.menu;
	private SystemColor labelColor = SystemColor.windowBorder;
	private SystemColor tipColor = SystemColor.windowBorder;
	private SystemColor borderColor = SystemColor.windowBorder;
	private SystemColor textColor = SystemColor.controlShadow;
	private SystemColor buttonColor = SystemColor.activeCaptionBorder;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConformanceView frame = new ConformanceView();

					frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void configFilterFile() {
		FileFilter autFilter = new FileTypeFilter(".aut", "Aut Files");
		fc.addChoosableFileFilter(autFilter);
		fc.setAcceptAllFileFilterUsed(false);
	}

	public void cleanVeredict() {
		lbl_veredict_ioco.setText("");
		btnTestCases_ioco.setVisible(false);
		lbl_veredict_lang.setText("");
		btnTestCases_lang.setVisible(false);

		taTestCasesIoco.setText("");
		taTestCasesLang.setText("");
	}

	public void getImplementationPath() {
		failPath = "";
		cleanVeredict();
		try {
			configFilterFile();
			fc.showOpenDialog(ConformanceView.this);
			tfImplementation.setText(fc.getSelectedFile().getName());
			pathImplementation = fc.getSelectedFile().getAbsolutePath();
			fc.setCurrentDirectory(fc.getSelectedFile().getParentFile());
			lblImplementationIoco.setText(tfImplementation.getText());
			lblimplementationLang.setText(tfImplementation.getText());
			// processModels(true, ioco);
			isImplementationProcess = false;

			Frame[] allFrames = Frame.getFrames();
			for (Frame frame : allFrames) {
				if (frame.getTitle().startsWith(ViewConstants.titleFrameImgImplementation)) {
					showImplementationImage = true;
					frame.setVisible(false);
					frame.dispose();
				}

				if (frame.getTitle().startsWith(ViewConstants.titleFrameImgSpecification)) {
					showSpecificationImage = true;
					frame.setVisible(false);
					frame.dispose();
				}
			}
		} catch (Exception e) {

		}
	}

	public void getSpecificationPath() {

		failPath = "";
		cleanVeredict();
		try {
			configFilterFile();
			fc.showOpenDialog(ConformanceView.this);
			tfSpecification.setText(fc.getSelectedFile().getName());
			pathSpecification = fc.getSelectedFile().getAbsolutePath();
			lblmodelIoco.setText(tfSpecification.getText());
			lblmodelLang.setText(tfSpecification.getText());
			isModelProcess = false;

			Frame[] allFrames = Frame.getFrames();
			for (Frame frame : allFrames) {
				if (frame.getTitle().startsWith(ViewConstants.titleFrameImgSpecification)) {
					showSpecificationImage = true;
					frame.setVisible(false);
					frame.dispose();
				}
			}

		} catch (Exception e) {
		}

	}

	public boolean regexIsValid(String exp) {
		try {
			RegExp regExp = new RegExp(exp);
			regExp.toAutomaton();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	final JPanel panel = new JPanel();

	public void actionVerifyConformance(boolean ioco) {
		long startTime = System.nanoTime();

		//lblWarningIoco.setText("");
		//lblWarningLang.setText("");
		if (isFormValid(ioco)) {// isFormValid(ioco)
			if (ioco) {
				iocoConformance();
			} else {
				languageBasedConformance();
			}

			if (S.getTransitions().size() > 0 || I.getTransitions().size() > 0) {
				if (ioco) {
					lbl_veredict_ioco.setText(Operations.veredict(conformidade));
					if (!failPath.equals("")) {
						taTestCasesIoco.setText(failPath);
						// btnTestCases_ioco.setVisible(true);
					}
				} else {
					lbl_veredict_lang.setText(Operations.veredict(conformidade));
					if (!failPath.equals("")) {
						// btnTestCases_lang.setVisible(true);
						taTestCasesLang.setText(failPath);
					}
				}
			}

		} /*else {
			errorMessage(ioco);
		}*/

		long endTime = System.nanoTime();
		long totalTime = endTime - startTime;

		long convert = TimeUnit.SECONDS.convert(totalTime, TimeUnit.NANOSECONDS);
		// convert = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);

		System.out.println(convert);

	}

	BufferedImage pathImageModel = null;
	BufferedImage pathImageImplementation = null;

	public void enableShowImage(boolean lts, boolean implementation ) throws Exception {

		if (((!tfImplementation.getText().isEmpty() && implementation)
				|| (!tfSpecification.getText().isEmpty() && !implementation))
				&& ((cbModel.getSelectedItem() == ViewConstants.IOLTS_CONST
						&& ((cbLabel.getSelectedItem() == ViewConstants.typeAutomaticLabel)
								|| (cbLabel.getSelectedItem() == ViewConstants.typeManualLabel
										&& !tfInput.getText().isEmpty() && !tfOutput.getText().isEmpty()))))
				|| lts) {
			setModel(lts, implementation);
			if (implementation && I.getTransitions().size() > 0) {
				pathImageImplementation = ModelImageGenerator.generateImage(I);
				if (pathImageImplementation != null) {
					btnViewImplementationIoco.setVisible(true);
					btnViewImplementationLang.setVisible(true);
					btnViewImplementationIoco.setEnabled(true);
					btnViewImplementationLang.setEnabled(true);
				} else {
					btnViewImplementationIoco.setVisible(true);
					btnViewImplementationLang.setVisible(true);
					btnViewImplementationIoco.setEnabled(false);
					btnViewImplementationLang.setEnabled(false);
				}

			} else {
				if(S.getTransitions().size() > 0) {
					pathImageModel = ModelImageGenerator.generateImage(S);
					if (pathImageModel != null) {
						btnViewModelIoco.setVisible(true);
						btnViewModelLang.setVisible(true);
						btnViewModelIoco.setEnabled(true);
						btnViewModelLang.setEnabled(true);
					} else {
						btnViewModelIoco.setVisible(true);
						btnViewModelLang.setVisible(true);
						btnViewModelIoco.setEnabled(false);
						btnViewModelLang.setEnabled(false);
					}
				}
				
			}
		} else {
			if (implementation) {
				btnViewImplementationIoco.setVisible(true);
				btnViewImplementationLang.setVisible(true);
				btnViewImplementationIoco.setEnabled(false);
				btnViewImplementationLang.setEnabled(false);

				isImplementationProcess = false;
			} else {
				btnViewModelIoco.setVisible(true);
				btnViewModelLang.setVisible(true);
				btnViewModelIoco.setEnabled(false);
				btnViewModelLang.setEnabled(false);

				isModelProcess = false;
			}
		}

	}

	public void setModel(boolean lts, boolean implementation) throws Exception {
		LTS S_ = new LTS(), I_ = new LTS();

		if (lts) {
			if (!implementation) {
				S_ = ImportAutFile.autToLTS(pathSpecification);

			} else {
				I_ = ImportAutFile.autToLTS(pathImplementation);
			}
			tfInput.setText(StringUtils.join(S_.getAlphabet(), ","));
		}

		if (cbLabel.getSelectedIndex() == 2 || lts) {// manual input/output
			if (!implementation) {
				S = ImportAutFile.autToIOLTS(pathSpecification, true,
						new ArrayList<String>(Arrays.asList(tfInput.getText().split(","))),
						new ArrayList<String>(Arrays.asList(tfOutput.getText().split(","))));

			} else {
				I = ImportAutFile.autToIOLTS(pathImplementation, true,
						new ArrayList<String>(Arrays.asList(tfInput.getText().split(","))),
						new ArrayList<String>(Arrays.asList(tfOutput.getText().split(","))));

			}

			if(lts) {
				tfInput.setText("");
			}
						
			//lblWarningIoco.setText("");
			//lblWarningLang.setText("");
		} else {// ?/!
			if (!implementation) {
				S = ImportAutFile.autToIOLTS(pathSpecification, false, new ArrayList<String>(),
						new ArrayList<String>());

			} else {
				I = ImportAutFile.autToIOLTS(pathImplementation, false, new ArrayList<String>(),
						new ArrayList<String>());

			}

			if (implementation) {

				if (I.getTransitions().size() == 0) {
					if (!lblWarningIoco.getText().contains(ViewConstants.msgImp)) {
						lblWarningIoco.setText(lblWarningIoco.getText() + ViewConstants.msgImp);
					}

					if (!lblWarningLang.getText().contains(ViewConstants.msgImp)) {
						lblWarningLang.setText(lblWarningLang.getText() + ViewConstants.msgImp);
					}

				}else {
					removeMessage(true, ViewConstants.msgImp);
					removeMessage(false, ViewConstants.msgImp);					
				}
			} else {

				if (S.getTransitions().size() == 0) {
					if (!lblWarningIoco.getText().contains(ViewConstants.msgModel)) {
						lblWarningIoco.setText(lblWarningIoco.getText() + ViewConstants.msgModel);
					}
					if (!lblWarningLang.getText().contains(ViewConstants.msgModel)) {
						lblWarningLang.setText(lblWarningLang.getText() + ViewConstants.msgModel);
					}
				}else {
					removeMessage(true, ViewConstants.msgModel);
					removeMessage(false, ViewConstants.msgModel);					
				}
			}

		}

	}

	public void processModels(boolean implementation, boolean ioco) throws Exception {

		boolean lts = false;

		if (cbModel.getSelectedIndex() == 0
				|| (cbLabel.getSelectedIndex() == 0 && cbModel.getSelectedItem() == ViewConstants.IOLTS_CONST)
				|| cbModel.getSelectedItem() == ViewConstants.LTS_CONST
				|| (cbLabel.getSelectedItem() == ViewConstants.typeManualLabel && tfInput.getText().isEmpty()
						&& tfOutput.getText().isEmpty())) {
			lts = true;
		}

		if (!implementation) {
			isModelProcess = true;
		} else {
			isImplementationProcess = true;
		}

		enableShowImage(lts, implementation);

		try {
			if ((ioco && isFormValid(ioco)) || (!ioco && isFormValid(ioco))) {

				if (lts) {
					showModelLabel(true, (implementation) ? I : S);
				} else {
					showModelLabel(false, (implementation) ? I : S);
				}

			} else {
				if (lts) {
					showModelLabel(true);
				} else {
					showModelLabel(false);
				}
			}

		} catch (Exception e) {

		}

	}

	public void showModelLabel(boolean label) {
		lblLabel.setVisible(label);
		lblLabelIoco.setVisible(label);
		lblLabel_.setVisible(label);
		lblLabelLang.setVisible(label);

		lblnput.setVisible(!label);
		lblOutput_.setVisible(!label);
		lblInput_.setVisible(!label);
		lblOutput_1.setVisible(!label);

		lblInputIoco.setVisible(!label);
		lblOutputIoco.setVisible(!label);
		lblInputLang.setVisible(!label);
		lblOutputLang.setVisible(!label);
	}

	public void showModelLabel(boolean lts, IOLTS model) {
		if (lts) {
			showModelLabel(true);

			lblLabelIoco.setText(StringUtils.join(model.getAlphabet(), ","));
			lblLabelLang.setText(StringUtils.join(model.getAlphabet(), ","));
		} else {
			showModelLabel(false);
			lblInputIoco.setText(StringUtils.join(model.getInputs(), ","));
			lblOutputIoco.setText(StringUtils.join(model.getOutputs(), ","));

			lblInputLang.setText(StringUtils.join(model.getInputs(), ","));
			lblOutputLang.setText(StringUtils.join(model.getOutputs(), ","));
		}
	}

	boolean isModelProcess = false;
	boolean isImplementationProcess = false;

	/**
	 * Create the frame.
	 */
	public ConformanceView() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/img/icon.PNG")));
		setTitle(ViewConstants.toolName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 833, 540);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new GridLayout(1, 0, 0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				boolean ioco = false;
				String tab = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
				if (tab.equals(ViewConstants.tabIOCO)) {
					ioco = true;
				} else {
					if (tab.equals(ViewConstants.tabLang)) {
						ioco = false;
					}
				}

				
				
				if (tab.equals(ViewConstants.tabIOCO) || tab.equals(ViewConstants.tabLang)) {
					try {
						if (!isFormValid(ioco)) {
							errorMessage(ioco);							
						}else {
							lblWarningIoco.setText("");
							lblWarningLang.setText("");
						}
						
						if (!isModelProcess) {
							processModels(false, ioco);
						}
						if (!isImplementationProcess) {
							processModels(true, ioco);
						}
					} catch (Exception e) {
					}
				}

			}
		});

		tabbedPane.setBackground(backgroundColor);
		tabbedPane.setFont(new Font("Microsoft YaHei Light", Font.PLAIN, 13));
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel panel_conf = new JPanel();
		panel_conf.setForeground(SystemColor.textInactiveText);
		panel_conf.setBackground(backgroundColor);
		panel_conf.setToolTipText("");
		tabbedPane.addTab("Configuration", null, panel_conf, null);
		panel_conf.setLayout(null);

		cbModel = new JComboBox();
		cbModel.setForeground(textColor);
		cbModel.setBackground(backgroundColor);
		cbModel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				actionCbModel(arg0.getItem().toString());
			}
		});

		cbModel.setModel(new DefaultComboBoxModel(ViewConstants.models));
		cbModel.setFont(new Font("Dialog", Font.BOLD, 13));
		cbModel.setBounds(37, 167, 324, 26);
		cbModel.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		panel_conf.add(cbModel);

		lblImplementation = new JLabel("Implementation");
		lblImplementation.setBackground(backgroundColor);
		lblImplementation.setForeground(labelColor);
		lblImplementation.setFont(new Font("Dialog", Font.BOLD, 13));
		lblImplementation.setBounds(37, 66, 157, 22);
		panel_conf.add(lblImplementation);

		lblSpecification = new JLabel("Model");
		lblSpecification.setForeground(SystemColor.controlDkShadow);
		lblSpecification.setFont(new Font("Dialog", Font.BOLD, 13));
		lblSpecification.setBounds(37, 11, 223, 14);
		panel_conf.add(lblSpecification);

		tfImplementation = new JTextField();
		tfImplementation.setForeground(textColor);
		tfImplementation.setBackground(backgroundColor);
		tfImplementation.setToolTipText("accepts only .aut files");
		tfImplementation.setFont(new Font("Dialog", Font.BOLD, 13));
		tfImplementation.addMouseListener(new MouseAdapter() {
			/*
			 * @Override public void mouseClicked(MouseEvent arg0) {
			 * getImplementationPath(); }
			 */
			@Override
			public void mousePressed(MouseEvent e) {
				getImplementationPath();

			}
		});
		tfImplementation.setColumns(10);
		tfImplementation.setBounds(37, 93, 700, 26);
		tfImplementation.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		panel_conf.add(tfImplementation);

		tfSpecification = new JTextField();
		tfSpecification.setForeground(textColor);
		tfSpecification.setBackground(backgroundColor);
		tfSpecification.setToolTipText("accepts only .aut files");
		tfSpecification.setFont(new Font("Dialog", Font.BOLD, 13));
		tfSpecification.addMouseListener(new MouseAdapter() {
			/*
			 * @Override public void mouseClicked(MouseEvent e) { getSpecificationPath(); }
			 */
			@Override
			public void mousePressed(MouseEvent e) {
				getSpecificationPath();

			}
		});
		tfSpecification.setColumns(10);
		tfSpecification.setBounds(37, 29, 700, 26);
		tfSpecification.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		panel_conf.add(tfSpecification);

		JButton btnFolderImp = new JButton("");
		btnFolderImp.setBackground(buttonColor);
		btnFolderImp.setOpaque(true);
		btnFolderImp.addMouseListener(new MouseAdapter() {
			/*
			 * @Override public void mouseClicked(MouseEvent e) { getImplementationPath(); }
			 */
			@Override
			public void mousePressed(MouseEvent e) {
				getImplementationPath();

			}
		});
		btnFolderImp.setIcon(new ImageIcon(this.getClass().getResource(ViewConstants.folderIconPath)));
		btnFolderImp.setBounds(736, 93, 39, 28);
		panel_conf.add(btnFolderImp);

		JButton btnFolderSpec = new JButton("");
		btnFolderSpec.setBackground(buttonColor);
		btnFolderSpec.setOpaque(true);
		btnFolderSpec.addMouseListener(new MouseAdapter() {
			/*
			 * @Override public void mouseClicked(MouseEvent e) { getSpecificationPath(); }
			 */
			@Override
			public void mousePressed(MouseEvent e) {
				getSpecificationPath();

			}
		});
		btnFolderSpec.setIcon(new ImageIcon(this.getClass().getResource(ViewConstants.folderIconPath)));
		btnFolderSpec.setBounds(736, 29, 39, 28);
		panel_conf.add(btnFolderSpec);

		lblOutput = new JLabel("Output");
		lblOutput.setForeground(labelColor);
		lblOutput.setFont(new Font("Dialog", Font.BOLD, 13));
		lblOutput.setBounds(37, 307, 48, 22);
		lblOutput.setVisible(false);
		panel_conf.add(lblOutput);

		lblInput = new JLabel("Input");
		lblInput.setForeground(labelColor);
		lblInput.setFont(new Font("Dialog", Font.BOLD, 13));
		lblInput.setBounds(37, 227, 54, 22);
		lblInput.setVisible(false);
		panel_conf.add(lblInput);

		tfInput = new JTextField();
		tfInput.setForeground(textColor);
		tfInput.setBackground(backgroundColor);
		tfInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				failPath = "";
				cleanVeredict();
				isModelProcess = false;
				isImplementationProcess = false;
			}
		});
		tfInput.setToolTipText("");
		tfInput.setFont(new Font("Dialog", Font.BOLD, 13));
		tfInput.setColumns(10);
		tfInput.setBounds(37, 247, 738, 22);
		tfInput.setVisible(false);

		tfInput.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		panel_conf.add(tfInput);

		tfOutput = new JTextField();
		tfOutput.setForeground(textColor);
		tfOutput.setBackground(backgroundColor);
		tfOutput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				failPath = "";
				cleanVeredict();
				isModelProcess = false;
				isImplementationProcess = false;
			}
		});
		tfOutput.setFont(new Font("Dialog", Font.BOLD, 13));
		tfOutput.setColumns(10);
		tfOutput.setBounds(37, 327, 738, 22);
		tfOutput.setVisible(false);
		tfOutput.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		panel_conf.add(tfOutput);

		lblLabelInp = new JLabel("(label split by comma)");
		lblLabelInp.setBackground(backgroundColor);
		lblLabelInp.setForeground(tipColor);
		lblLabelInp.setFont(new Font("Dialog", Font.BOLD, 12));
		lblLabelInp.setBounds(651, 280, 173, 14);
		lblLabelInp.setVisible(false);
		panel_conf.add(lblLabelInp);

		lblLabelOut = new JLabel("(label split by comma)");
		lblLabelOut.setBackground(backgroundColor);
		lblLabelOut.setForeground(tipColor);
		lblLabelOut.setFont(new Font("Dialog", Font.BOLD, 12));
		lblLabelOut.setBounds(651, 358, 164, 14);
		lblLabelOut.setVisible(false);
		panel_conf.add(lblLabelOut);

		JList list = new JList();
		list.setBounds(371, 25, 1, 1);
		panel_conf.add(list);

		lblRotulo = new JLabel("Label");
		lblRotulo.setForeground(labelColor);
		lblRotulo.setFont(new Font("Dialog", Font.BOLD, 13));
		lblRotulo.setBounds(451, 143, 274, 22);
		lblRotulo.setVisible(false);
		panel_conf.add(lblRotulo);

		cbLabel = new JComboBox();
		cbLabel.setVisible(false);
		cbLabel.setForeground(textColor);
		cbLabel.setBackground(backgroundColor);
		cbLabel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				actionCbLabel(arg0.getItem().toString());
			}
		});
		cbLabel.setModel(new DefaultComboBoxModel(
				new String[] { "", ViewConstants.typeAutomaticLabel, ViewConstants.typeManualLabel }));
		cbLabel.setFont(new Font("Dialog", Font.BOLD, 13));
		cbLabel.setBounds(451, 167, 324, 26);
		// cbLabel.setVisible(false);
		cbLabel.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		panel_conf.add(cbLabel);

		JLabel lblIolts = new JLabel("Model:");
		lblIolts.setForeground(SystemColor.windowBorder);
		lblIolts.setFont(new Font("Dialog", Font.BOLD, 13));
		lblIolts.setBounds(37, 147, 144, 14);
		panel_conf.add(lblIolts);

		panel_ioco = new JPanel();
		tabbedPane.addTab(ViewConstants.tabIOCO, null, panel_ioco, null);
		panel_ioco.setLayout(null);

		btnVerifyConf_ioco = new JButton("Verify");
		btnVerifyConf_ioco.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				actionVerifyConformance(true);
			}
		});
		btnVerifyConf_ioco.setBounds(10, 130, 167, 44);
		btnVerifyConf_ioco.setFont(new Font("Dialog", Font.BOLD, 13));
		btnVerifyConf_ioco.setBackground(Color.LIGHT_GRAY);
		panel_ioco.add(btnVerifyConf_ioco);

		btnTestCases_ioco = new JButton("Show test cases");
		btnTestCases_ioco.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showTestCases();
			}
		});
		btnTestCases_ioco.setFont(new Font("Dialog", Font.BOLD, 13));
		btnTestCases_ioco.setBackground(Color.LIGHT_GRAY);
		btnTestCases_ioco.setBounds(671, 130, 105, 44);
		panel_ioco.add(btnTestCases_ioco);

		lbl_veredict_ioco = new JLabel("");
		lbl_veredict_ioco.setForeground(SystemColor.windowBorder);
		lbl_veredict_ioco.setFont(new Font("Dialog", Font.BOLD, 13));
		lbl_veredict_ioco.setBounds(187, 142, 474, 20);
		panel_ioco.add(lbl_veredict_ioco);

		JLabel lblModel = new JLabel("Model");
		lblModel.setForeground(SystemColor.windowBorder);
		lblModel.setFont(new Font("Dialog", Font.BOLD, 13));
		lblModel.setBounds(37, 11, 52, 14);
		panel_ioco.add(lblModel);

		JLabel lblImplementation_1 = new JLabel("Implementation");
		lblImplementation_1.setForeground(SystemColor.windowBorder);
		lblImplementation_1.setFont(new Font("Dialog", Font.BOLD, 13));
		lblImplementation_1.setBounds(425, 11, 105, 22);
		panel_ioco.add(lblImplementation_1);

		lblnput = new JLabel("Input");
		lblnput.setForeground(SystemColor.windowBorder);
		lblnput.setFont(new Font("Dialog", Font.BOLD, 13));
		lblnput.setBounds(37, 71, 44, 14);
		panel_ioco.add(lblnput);

		lblOutput_1 = new JLabel("Output");
		lblOutput_1.setForeground(SystemColor.windowBorder);
		lblOutput_1.setFont(new Font("Dialog", Font.BOLD, 13));
		lblOutput_1.setBounds(425, 71, 67, 14);
		panel_ioco.add(lblOutput_1);

		lblmodelIoco = new JLabel("");
		lblmodelIoco.setForeground(SystemColor.controlShadow);
		lblmodelIoco.setBounds(37, 29, 265, 26);
		panel_ioco.add(lblmodelIoco);

		lblImplementationIoco = new JLabel("");
		lblImplementationIoco.setForeground(SystemColor.controlShadow);
		lblImplementationIoco.setBounds(425, 34, 367, 26);
		panel_ioco.add(lblImplementationIoco);

		lblInputIoco = new JLabel("");
		lblInputIoco.setForeground(SystemColor.controlShadow);
		lblInputIoco.setBounds(37, 88, 378, 26);
		panel_ioco.add(lblInputIoco);

		lblOutputIoco = new JLabel("");
		lblOutputIoco.setForeground(SystemColor.controlShadow);
		lblOutputIoco.setBounds(425, 88, 367, 26);
		panel_ioco.add(lblOutputIoco);

		taTestCasesIoco = new JTextArea();
		taTestCasesIoco.setBounds(10, 277, 231, 150);
		JScrollPane scrolltxt = new JScrollPane(taTestCasesIoco);
		scrolltxt.setBounds(10, 201, 405, 247);
		panel_ioco.add(scrolltxt);

		imgModelIoco = new JLabel("");
		imgModelIoco.setVisible(false);
		imgModelIoco.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				// showModelImage(false);
			}
		});
		imgModelIoco.setBounds(469, 130, 44, 44);
		imgImplementationIoco = new JLabel("");
		imgImplementationIoco.setVisible(false);
		// imageShowHide(false, true);
		panel_ioco.add(imgModelIoco);

		imgImplementationIoco.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// showModelImage(true);
			}
		});
		imgImplementationIoco.setBounds(530, 130, 44, 44);

		panel_ioco.add(imgImplementationIoco);

		btnViewModelIoco = new JButton("view the model");
		btnViewModelIoco.setVisible(false);
		btnViewModelIoco.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (showSpecificationImage && btnViewModelIoco.isEnabled()) {
					showModelImage(false);
				}

			}
		});
		btnViewModelIoco.setFont(new Font("Dialog", Font.BOLD, 13));
		btnViewModelIoco.setBackground(Color.LIGHT_GRAY);
		btnViewModelIoco.setBounds(113, 5, 154, 26);
		panel_ioco.add(btnViewModelIoco);

		btnViewImplementationIoco = new JButton("view the model");
		btnViewImplementationIoco.setVerticalAlignment(SwingConstants.BOTTOM);
		btnViewImplementationIoco.setVisible(false);
		btnViewImplementationIoco.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (showImplementationImage && btnViewImplementationIoco.isEnabled()) {
					showModelImage(true);
				}
			}
		});
		btnViewImplementationIoco.setFont(new Font("Dialog", Font.BOLD, 13));
		btnViewImplementationIoco.setBackground(Color.LIGHT_GRAY);
		btnViewImplementationIoco.setBounds(568, 5, 154, 26);
		panel_ioco.add(btnViewImplementationIoco);

		lblLabel = new JLabel("Label");
		lblLabel.setVisible(false);
		lblLabel.setForeground(SystemColor.windowBorder);
		lblLabel.setFont(new Font("Dialog", Font.BOLD, 13));
		lblLabel.setBounds(37, 71, 44, 14);
		panel_ioco.add(lblLabel);

		lblLabelIoco = new JLabel("");
		lblLabelIoco.setForeground(SystemColor.controlShadow);
		lblLabelIoco.setBounds(37, 88, 755, 26);
		panel_ioco.add(lblLabelIoco);

		JLabel lblWarnings = new JLabel("Warnings");
		lblWarnings.setForeground(SystemColor.windowBorder);
		lblWarnings.setFont(new Font("Dialog", Font.BOLD, 13));
		lblWarnings.setBounds(425, 179, 67, 14);
		panel_ioco.add(lblWarnings);

		lblWarningIoco = new TextArea("");
		lblWarningIoco.setForeground(SystemColor.controlShadow);
		lblWarningIoco.setBounds(425, 201, 367, 247);
		panel_ioco.add(lblWarningIoco);

		panel_language = new JPanel();
		tabbedPane.addTab(ViewConstants.tabLang, null, panel_language, null);
		panel_language.setLayout(null);

		lblD = new JLabel("Desirable behavior");
		lblD.setForeground(SystemColor.windowBorder);
		lblD.setFont(new Font("Dialog", Font.BOLD, 13));
		lblD.setBounds(37, 125, 244, 20);
		panel_language.add(lblD);

		tfD = new JTextField();
		tfD.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				cleanVeredict();
			}
		});
		tfD.setForeground(SystemColor.controlShadow);
		tfD.setFont(new Font("Dialog", Font.BOLD, 13));
		tfD.setColumns(10);
		tfD.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		tfD.setBackground(SystemColor.menu);
		tfD.setBounds(37, 152, 352, 26);
		panel_language.add(tfD);

		lblRegexD = new JLabel("(Regex: +,*,())");
		lblRegexD.setForeground(SystemColor.windowBorder);
		lblRegexD.setFont(new Font("Dialog", Font.BOLD, 12));
		lblRegexD.setBounds(306, 179, 93, 36);
		panel_language.add(lblRegexD);

		lblF = new JLabel("Failure property");
		lblF.setForeground(SystemColor.windowBorder);
		lblF.setFont(new Font("Dialog", Font.BOLD, 13));
		lblF.setBounds(425, 128, 200, 14);
		panel_language.add(lblF);

		tfF = new JTextField();
		tfF.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				cleanVeredict();
			}
		});
		tfF.setForeground(SystemColor.controlShadow);
		tfF.setFont(new Font("Dialog", Font.BOLD, 13));
		tfF.setColumns(10);
		tfF.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		tfF.setBackground(SystemColor.menu);
		tfF.setBounds(425, 152, 367, 26);
		panel_language.add(tfF);

		lblRegexF = new JLabel("(Regex: +,*,())");
		lblRegexF.setForeground(SystemColor.windowBorder);
		lblRegexF.setFont(new Font("Dialog", Font.BOLD, 12));
		lblRegexF.setBounds(709, 179, 93, 36);
		panel_language.add(lblRegexF);

		btnTestCases_lang = new JButton("Show test cases");
		btnTestCases_lang.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showTestCases();
			}
		});
		btnTestCases_lang.setFont(new Font("Dialog", Font.BOLD, 13));
		btnTestCases_lang.setBackground(Color.LIGHT_GRAY);
		btnTestCases_lang.setBounds(714, 225, 78, 46);
		btnTestCases_lang.setEnabled(false);
		panel_language.add(btnTestCases_lang);

		lbl_veredict_lang = new JLabel("");
		lbl_veredict_lang.setForeground(SystemColor.windowBorder);
		lbl_veredict_lang.setFont(new Font("Dialog", Font.BOLD, 13));
		lbl_veredict_lang.setBounds(198, 235, 493, 20);
		panel_language.add(lbl_veredict_lang);

		btnVerifyConf_lang = new JButton("Verify");
		btnVerifyConf_lang.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				actionVerifyConformance(false);
			}
		});
		btnVerifyConf_lang.setFont(new Font("Dialog", Font.BOLD, 13));
		btnVerifyConf_lang.setBackground(Color.LIGHT_GRAY);
		btnVerifyConf_lang.setBounds(11, 226, 167, 44);
		panel_language.add(btnVerifyConf_lang);

		label_1 = new JLabel("Model");
		label_1.setForeground(SystemColor.windowBorder);
		label_1.setFont(new Font("Dialog", Font.BOLD, 13));
		label_1.setBounds(37, 11, 52, 14);
		panel_language.add(label_1);

		lblInput_ = new JLabel("Input");
		lblInput_.setForeground(SystemColor.windowBorder);
		lblInput_.setFont(new Font("Dialog", Font.BOLD, 13));
		lblInput_.setBounds(37, 71, 44, 14);
		panel_language.add(lblInput_);

		lblInputLang = new JLabel("");
		lblInputLang.setForeground(SystemColor.controlShadow);
		lblInputLang.setBounds(37, 91, 378, 26);
		panel_language.add(lblInputLang);

		lblmodelLang = new JLabel("");
		lblmodelLang.setForeground(SystemColor.controlShadow);
		lblmodelLang.setBounds(37, 29, 352, 26);
		panel_language.add(lblmodelLang);

		label_5 = new JLabel("Implementation");
		label_5.setForeground(SystemColor.windowBorder);
		label_5.setFont(new Font("Dialog", Font.BOLD, 13));
		label_5.setBounds(425, 11, 105, 22);
		panel_language.add(label_5);

		lblimplementationLang = new JLabel("");
		lblimplementationLang.setForeground(SystemColor.controlShadow);
		lblimplementationLang.setBounds(425, 29, 367, 26);
		panel_language.add(lblimplementationLang);

		lblOutput_ = new JLabel("Output");
		lblOutput_.setForeground(SystemColor.windowBorder);
		lblOutput_.setFont(new Font("Dialog", Font.BOLD, 13));
		lblOutput_.setBounds(425, 71, 67, 14);
		panel_language.add(lblOutput_);

		lblOutputLang = new JLabel("");
		lblOutputLang.setForeground(SystemColor.controlShadow);
		lblOutputLang.setBounds(425, 88, 367, 26);
		panel_language.add(lblOutputLang);

		/*
		 * taTestCasesLang = new JTextArea(); taTestCasesLang.setBounds(10, 282, 584,
		 * 197); panel_language.add(taTestCasesLang);
		 */

		taTestCasesLang = new JTextArea();
		taTestCasesLang.setEditable(false);
		taTestCasesLang.setBounds(10, 282, 584, 197);
		// taTestCasesLang.enable(false);
		JScrollPane scrolltxt2 = new JScrollPane(taTestCasesLang);
		scrolltxt2.setBounds(11, 293, 405, 160);

		panel_language.add(scrolltxt2);

		imgModelLang = new JLabel("");
		imgModelLang.setVisible(false);
		imgModelLang.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// showModelImage(false);
			}
		});
		imgModelLang.setBounds(678, 235, 44, 36);
		imgImplementationLang = new JLabel("");
		imgImplementationLang.setVisible(false);
		// imageShowHide(false, false);
		panel_language.add(imgModelLang);
		imgImplementationLang.setVisible(false);
		imgImplementationLang.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// showModelImage(true);
			}
		});
		imgImplementationLang.setBounds(660, 235, 44, 36);
		panel_language.add(imgImplementationLang);

		btnViewModelLang = new JButton("view the model");
		btnViewModelLang.setVisible(false);
		btnViewModelLang.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (showSpecificationImage && btnViewModelLang.isEnabled()) {
					showModelImage(false);
				}
			}
		});
		btnViewModelLang.setFont(new Font("Dialog", Font.BOLD, 13));
		btnViewModelLang.setBackground(Color.LIGHT_GRAY);
		btnViewModelLang.setBounds(113, 5, 154, 26);
		panel_language.add(btnViewModelLang);

		btnViewImplementationLang = new JButton("view the model");
		btnViewImplementationLang.setVisible(false);
		btnViewImplementationLang.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (showImplementationImage && btnViewImplementationLang.isEnabled()) {
					showModelImage(true);
				}
			}
		});
		btnViewImplementationLang.setFont(new Font("Dialog", Font.BOLD, 13));
		btnViewImplementationLang.setBackground(Color.LIGHT_GRAY);
		btnViewImplementationLang.setBounds(568, 5, 154, 26);
		panel_language.add(btnViewImplementationLang);

		lblLabelLang = new JLabel("");
		lblLabelLang.setForeground(SystemColor.controlShadow);
		lblLabelLang.setBounds(37, 88, 755, 26);
		panel_language.add(lblLabelLang);

		lblLabel_ = new JLabel("Label");
		lblLabel_.setVisible(false);
		lblLabel_.setForeground(SystemColor.windowBorder);
		lblLabel_.setFont(new Font("Dialog", Font.BOLD, 13));
		lblLabel_.setBounds(37, 71, 44, 14);
		panel_language.add(lblLabel_);

		JLabel label = new JLabel("Warnings");
		label.setForeground(SystemColor.windowBorder);
		label.setFont(new Font("Dialog", Font.BOLD, 13));
		label.setBounds(426, 276, 67, 14);
		panel_language.add(label);

		lblWarningLang = new TextArea("");
		lblWarningLang.setForeground(SystemColor.controlShadow);
		lblWarningLang.setBounds(426, 293, 366, 160);
		panel_language.add(lblWarningLang);

		cleanVeredict();
	}

	boolean showImplementationImage = true;
	boolean showSpecificationImage = true;

	public void showModelImage(boolean implementation) {

		int size = 550;

		BufferedImage bimg;
		int width;
		int height;

		String model = (implementation) ? ViewConstants.titleFrameImgImplementation + tfImplementation.getText()
				: ViewConstants.titleFrameImgSpecification + tfSpecification.getText();

		try {
			JFrame frame = new JFrame(model);
			frame.setVisible(true);

			JPanel panel = new JPanel();

			JLabel jl = new JLabel();

			if (implementation) {
				// bimg = ImageIO.read(new File(pathImageImplementation));
				width = pathImageImplementation.getWidth();
				height = pathImageImplementation.getHeight();
				frame.setSize(width + 50, height + 50);

				jl.setIcon(new ImageIcon(new ImageIcon(pathImageImplementation).getImage().getScaledInstance(width,
						height, Image.SCALE_DEFAULT)));
				showImplementationImage = false;
			} else {
				// bimg = ImageIO.read(new File(pathImageModel));
				width = pathImageModel.getWidth();
				height = pathImageModel.getHeight();

				frame.setSize(width + 50, height + 50);
				jl.setIcon(new ImageIcon(new ImageIcon(pathImageModel).getImage().getScaledInstance(width, height,
						Image.SCALE_DEFAULT)));
				showSpecificationImage = false;
			}

			// if (implementation) {
			// bimg = ImageIO.read(new File(pathImageImplementation));
			// width = bimg.getWidth();
			// height = bimg.getHeight();
			// frame.setSize(width + 50, height + 50);
			//
			// jl.setIcon(new ImageIcon(new
			// ImageIcon(pathImageImplementation).getImage().getScaledInstance(width,
			// height, Image.SCALE_DEFAULT)));
			// } else {
			// bimg = ImageIO.read(new File(pathImageModel));
			// width = bimg.getWidth();
			// height = bimg.getHeight();
			//
			// frame.setSize(width + 50, height + 50);
			// jl.setIcon(new ImageIcon(new
			// ImageIcon(pathImageModel).getImage().getScaledInstance(width, height,
			// Image.SCALE_DEFAULT)));
			// }

			panel.add(jl);
			JScrollPane scrolltxt = new JScrollPane(panel);
			scrolltxt.setBounds(3, 3, width / (width % size), height / (height % size));
			// panel.add(scrolltxt);
			frame.getContentPane().add(scrolltxt);

			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				// when image closed
				@Override
				public void windowClosing(java.awt.event.WindowEvent windowEvent) {
					if (frame.getTitle().startsWith(ViewConstants.titleFrameImgImplementation)) {
						showImplementationImage = true;
					}

					if (frame.getTitle().startsWith(ViewConstants.titleFrameImgSpecification)) {
						showSpecificationImage = true;
					}

				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void setInputOutputField(boolean visibility) {
		lblInput.setVisible(visibility);
		lblOutput.setVisible(visibility);
		tfInput.setVisible(visibility);
		lblLabelInp.setVisible(visibility);
		lblLabelOut.setVisible(visibility);
		tfOutput.setVisible(visibility);
		tfInput.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));
		tfOutput.setBorder(new MatteBorder(0, 0, 1, 0, (Color) borderColor));

		// tfInput.setText("");
		// tfOutput.setText("");
		failPath = "";
		cleanVeredict();

	}

	public void actionCbLabel(String label) {
		if (label.equals(ViewConstants.typeManualLabel)) {
			setInputOutputField(true);
		} else {
			setInputOutputField(false);
		}
		isModelProcess = false;
		isImplementationProcess = false;
	}

	public void actionCbModel(String model) {
		if (model.equals(ViewConstants.IOLTS_CONST)) {
			cbLabel.setVisible(true);
			lblRotulo.setVisible(true);
			actionCbLabel(cbLabel.getSelectedItem().toString());
		} else {
			cbLabel.setVisible(false);
			lblRotulo.setVisible(false);
			setInputOutputField(false);
		}

		failPath = "";
		cleanVeredict();
		isModelProcess = false;
		isImplementationProcess = false;
	}

	public void setFieldRegex(boolean visibility) {
		lblD.setVisible(visibility);
		tfD.setVisible(visibility);
		lblRegexD.setVisible(visibility);
		lblF.setVisible(visibility);
		tfF.setVisible(visibility);
		lblRegexF.setVisible(visibility);
		tfD.setText("");
		tfF.setText("");
	}

	public class FileTypeFilter extends FileFilter {
		private String extension;
		private String description;

		public FileTypeFilter(String extension, String description) {
			this.extension = extension;
			this.description = description;
		}

		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			}
			return file.getName().endsWith(extension);
		}

		public String getDescription() {
			return description + String.format(" (*%s)", extension);
		}
	}

	Automaton_ conformidade = null;
	IOLTS S, I = null;

	public void iocoConformance() {
		conformidade = null;

		try {
			// if (cbLabel.getSelectedIndex() == 2) {// manual input/output
			// S = ImportAutFile.autToIOLTS(pathSpecification, true,
			// new ArrayList<String>(Arrays.asList(tfInput.getText().split(","))),
			// new ArrayList<String>(Arrays.asList(tfOutput.getText().split(","))));
			//
			// I = ImportAutFile.autToIOLTS(pathImplementation, true,
			// new ArrayList<String>(Arrays.asList(tfInput.getText().split(","))),
			// new ArrayList<String>(Arrays.asList(tfOutput.getText().split(","))));
			// } else {
			// S = ImportAutFile.autToIOLTS(pathSpecification, false, new
			// ArrayList<String>(),
			// new ArrayList<String>());
			//
			// I = ImportAutFile.autToIOLTS(pathImplementation, false, new
			// ArrayList<String>(),
			// new ArrayList<String>());
			// }

			if (S.getTransitions().size() == 0 || I.getTransitions().size() == 0) {
				if (S.getTransitions().size() == 0 && !constainsMessage(true, ViewConstants.msgModel)) {
					lblWarningIoco.setText(lblWarningIoco.getText() + ViewConstants.msgModel);
				}

				if (I.getTransitions().size() == 0 && !constainsMessage(true, ViewConstants.msgImp)) {
					lblWarningIoco.setText(lblWarningIoco.getText() + ViewConstants.msgImp);
				}

			} else {
				conformidade = IocoConformance.verifyIOCOConformance(S, I);
				failPath = Operations.path(S, I, conformidade, true);
			}

		} catch (Exception e_) {
			// JOptionPane.showMessageDialog(panel, e_.getMessage(), "Warning",
			// JOptionPane.WARNING_MESSAGE);
			lblWarningIoco.setText(ViewConstants.exceptionMessage);
			return;
		}
	}

	public void languageBasedConformance() {
		boolean lts = false;
		conformidade = null;
		// IOLTS S, I = null;
		LTS S_, I_ = null;
		try {

			// when the model type is not selected or IOLTS is selected but not specified
			// how to differentiate the inputs and outputs
			if (cbModel.getSelectedIndex() == 0
					|| (cbLabel.getSelectedIndex() == 0 && cbModel.getSelectedItem() == ViewConstants.IOLTS_CONST)
					|| cbModel.getSelectedItem() == ViewConstants.LTS_CONST
					|| (cbLabel.getSelectedItem() == ViewConstants.typeManualLabel && tfInput.getText().isEmpty()
							&& tfOutput.getText().isEmpty())) {
				lts = true;
			}

			if (!lts) { // IOLTS

				/*
				 * if (cbLabel.getSelectedIndex() == 2) {// manual input/output S =
				 * ImportAutFile.autToIOLTS(pathSpecification, true, new
				 * ArrayList<String>(Arrays.asList(tfInput.getText().split(","))), new
				 * ArrayList<String>(Arrays.asList(tfOutput.getText().split(","))));
				 * 
				 * I = ImportAutFile.autToIOLTS(pathImplementation, true, new
				 * ArrayList<String>(Arrays.asList(tfInput.getText().split(","))), new
				 * ArrayList<String>(Arrays.asList(tfOutput.getText().split(",")))); } else { S
				 * = ImportAutFile.autToIOLTS(pathSpecification, false, new ArrayList<String>(),
				 * new ArrayList<String>());
				 * 
				 * I = ImportAutFile.autToIOLTS(pathImplementation, false, new
				 * ArrayList<String>(), new ArrayList<String>()); }
				 */

				S_ = S.toLTS();
				I_ = I.toLTS();

			} else {
				S_ = ImportAutFile.autToLTS(pathSpecification);
				I_ = ImportAutFile.autToLTS(pathImplementation);
			}

			if (S_.getAlphabet().size() == 0 || I_.getAlphabet().size() == 0) {
				if (S_.getAlphabet().size() == 0
						&& !lblWarningLang.getText().contains(ViewConstants.msgModel)) {
					lblWarningLang.setText(lblWarningLang.getText() + ViewConstants.msgModel);
				}else {
					removeMessage(false, ViewConstants.msgModel);
				}

				if (I_.getAlphabet().size() == 0
						&& !lblWarningLang.getText().contains(ViewConstants.msgImp)) {
					lblWarningLang.setText(lblWarningLang.getText() + ViewConstants.msgImp);
				}else {
					removeMessage(false, ViewConstants.msgImp);
				}

			} else {
				String D = "";
				D = tfD.getText();
				if (tfD.getText().isEmpty() && tfF.getText().isEmpty()) {
					D = "(";
					for (String l : S_.getAlphabet()) {
						D += l + "+";
					}
					D = D.substring(0, D.length() - 1);
					D += ")*";

				}
				tfD.setText(D);
				String F = tfF.getText();

				if (regexIsValid(D) && regexIsValid(F)) {
					conformidade = LanguageBasedConformance.verifyLanguageConformance(S_, I_, D, F);
					failPath = Operations.path(S_, I_, conformidade, false);
					removeMessage(false, ViewConstants.invalidRegex);
				} else {
					// JOptionPane.showMessageDialog(panel, "Invalid regex!", "Warning",
					// JOptionPane.WARNING_MESSAGE);
					if (!lblWarningLang.getText().contains(ViewConstants.invalidRegex)) {
						lblWarningLang.setText(lblWarningLang.getText() + ViewConstants.invalidRegex);
					}

					return;
				}
			}

		} catch (Exception e_) {
			if (!lblWarningLang.getText().contains(ViewConstants.exceptionMessage)) {
				lblWarningLang.setText(lblWarningLang.getText() + ViewConstants.exceptionMessage);
			}

			// JOptionPane.showMessageDialog(panel, e_.getMessage(), "Warning",
			// JOptionPane.WARNING_MESSAGE);
			e_.printStackTrace();
			return;
		}
	}

	// public void imageShowHide(boolean show, boolean ioco) {
	//
	// if (!ioco) {
	// imgModelLang.setVisible(show);
	// imgModelLang.enable(show);
	// imgImplementationLang.setVisible(show);
	// imgImplementationLang.enable(show);
	// } else {
	// imgModelIoco.setVisible(show);
	// imgModelIoco.enable(show);
	// imgImplementationIoco.setVisible(show);
	// imgImplementationIoco.enable(show);
	// }
	//
	// }

	private JPanel panel_language;
	private JPanel panel_ioco;
	private JTextField tfD;
	private JTextField tfF;
	private JButton btnVerifyConf_ioco;
	private JButton btnTestCases_ioco;
	private JButton btnTestCases_lang;
	private JLabel lbl_veredict_lang;
	private JLabel lbl_veredict_ioco;
	private JButton btnVerifyConf_lang;
	private JLabel label_1;
	private JLabel lblInput_;
	private JLabel lblInputLang;
	private JLabel lblmodelLang;
	private JLabel label_5;
	private JLabel lblimplementationLang;
	private JLabel lblOutput_;
	private JLabel lblOutputLang;
	private JTextArea taTestCasesLang;
	private JLabel imgModelIoco;
	private JLabel imgImplementationIoco;
	private JLabel imgModelLang;
	private JLabel imgImplementationLang;
	private JLabel lblLabelLang;
	private JLabel lblLabel_;

	public boolean isFormValid(boolean ioco) {
		return (!tfImplementation.getText().isEmpty() && !tfSpecification.getText().isEmpty()// implementation and //
																								// specification field
				&& (cbModel.getSelectedIndex() != 0 || (!ioco || cbModel.getSelectedIndex() == 0)))
				&& (!ioco || (ioco && cbModel.getSelectedItem() == ViewConstants.IOLTS_CONST
						&& ((cbLabel.getSelectedItem() == ViewConstants.typeAutomaticLabel)
								|| (cbLabel.getSelectedItem() == ViewConstants.typeManualLabel
										&& !tfInput.getText().isEmpty() && !tfOutput.getText().isEmpty()))));// model
																												// selected
																												// (IOLTS
																												// or
																												// LTS)

		/*
		 * return (!tfImplementation.getText().isEmpty() &&
		 * !tfSpecification.getText().isEmpty()// implementation and // specification
		 * field && ((cbLabel.getSelectedItem() == typeAutomaticLabel) ||
		 * (cbLabel.getSelectedItem() == typeManualLabel && !tfInput.getText().isEmpty()
		 * && !tfOutput.getText().isEmpty())));// model selected (IOLTS or // LTS)
		 */
	}

	public boolean constainsMessage(boolean ioco, String msg) {
		if (ioco && lblWarningIoco.getText().contains(msg)) {
			return true;
		} else {
			if (lblWarningLang.getText().contains(msg)) {
				return true;
			}
		}

		return false;
	}

	public void removeMessage(boolean ioco, String msg) {
		if (ioco) {
			lblWarningIoco.setText(lblWarningIoco.getText().replaceAll(msg, ""));
		} else {
			lblWarningLang.setText(lblWarningLang.getText().replaceAll(msg, ""));
		}
	}

	public void errorMessage(boolean ioco) {
		// boolean langD = tfD.getText().isEmpty();
		// boolean langF = tfF.getText().isEmpty();
		boolean implementation = tfImplementation.getText().isEmpty();
		boolean specification = tfSpecification.getText().isEmpty();
		// boolean typeOfConf = (!rbConfBasedLang.isSelected() && !rbIoco.isSelected());

		boolean model = cbModel.getSelectedIndex() == 0;

		String msg = "";
		// msg += typeOfConf ? "Select the type of conformance [IOCO] or [Baseada em
		// Linguagem] \n" : "";

		

		if (!constainsMessage(ioco, ViewConstants.selectImplementation) && implementation) {
			msg += ViewConstants.selectImplementation;
		} else {
			if (!implementation) {
				removeMessage(ioco, ViewConstants.selectImplementation);
			}
		}

		if (!constainsMessage(ioco, ViewConstants.selectSpecification) && specification) {
			msg += ViewConstants.selectSpecification;
		} else {
			if (!specification) {
				removeMessage(ioco, ViewConstants.selectSpecification);
			}
		}
		
		if (!constainsMessage(ioco, ViewConstants.selectModel) && model) {
			msg += ViewConstants.selectModel;
		} else {
			if (!model) {
				removeMessage(ioco, ViewConstants.selectModel);
			}
		}
		// msg += langD && langF ? "The Language D field or F language is required \n" :
		// "";

		if (ioco) {
			boolean ioltsLabel = cbLabel.getSelectedIndex() == 0
					&& cbModel.getSelectedItem() == ViewConstants.IOLTS_CONST;

			if (!constainsMessage(ioco, ViewConstants.selectIoltsLabel) && ioltsLabel) {
				msg += ViewConstants.selectIoltsLabel;
			} else {
				if (!ioltsLabel) {
					removeMessage(ioco, ViewConstants.selectIoltsLabel);
				}
			}

			/*
			 * boolean ioltsLabel = cbLabel.getSelectedIndex() == 0; msg += ioltsLabel ?
			 * "It is necessary how the IOLTS labels will be distinguished \n" : "";
			 */

			boolean defInpuOut = (cbModel.getSelectedItem() == ViewConstants.IOLTS_CONST
					&& cbLabel.getSelectedItem() == ViewConstants.typeManualLabel && tfInput.getText().isEmpty()
					&& tfOutput.getText().isEmpty());

			if (!constainsMessage(ioco, ViewConstants.selectInpOut) && defInpuOut) {
				msg += ViewConstants.selectInpOut;
			} else {
				if (!defInpuOut) {
					removeMessage(ioco, ViewConstants.selectInpOut);
				}
			}

			/*
			 * boolean defInpuOut = (cbLabel.getSelectedItem() == typeManualLabel &&
			 * tfInput.getText().isEmpty() && tfOutput.getText().isEmpty());
			 */
			// msg += defInpuOut ? "The fields Input and Output is required \n": "";

			boolean lts = cbModel.getSelectedItem() == ViewConstants.LTS_CONST;

			if (!constainsMessage(ioco, ViewConstants.selectIolts) && lts) {
				msg += ViewConstants.selectIolts;
			} else {
				if (!lts) {
					removeMessage(ioco, ViewConstants.selectIolts);
				}
			}

		}

		if (ioco) {
			lblWarningIoco.setText(lblWarningIoco.getText() + msg);
		} else {
			lblWarningLang.setText(lblWarningLang.getText() + msg);
		}

		// JOptionPane.showMessageDialog(panel, msg, "Warning",
		// JOptionPane.WARNING_MESSAGE);
	}

	public void showTestCases() {
		JFrame frame = new JFrame();
		frame.setVisible(true);
		frame.setSize(500, 500);
		JPanel panel = new JPanel();
		TextArea ta = new TextArea(25, 60);

		ta.setText(failPath);
		JScrollPane scrolltxt = new JScrollPane(ta);
		scrolltxt.setBounds(3, 3, 400, 400);

		panel.add(scrolltxt);
		frame.getContentPane().add(panel);
	}
}