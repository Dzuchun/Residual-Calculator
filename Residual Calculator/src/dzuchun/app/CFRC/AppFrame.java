package dzuchun.app.CFRC;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.math3.complex.Complex;

import dzuchun.math.ComplexNumber;
import net.sourceforge.bracer.BracerParser;

public class AppFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final String APP_NAME = "Residual calculator";

	private static final String FUNCTION_HINT = "You may use functions like \"sin\", \"cos\", \"Ln\", \"tan\", \"Arcsin\", \"Arccos\", \"Arctan\". Available binary operations: +, -, *, /, ^. Please, use \"()\" as much, as you can: \"(z+1)\"";
	private static final String Z0_HINT = "A point residual is evaluated at.";
	private static final String R0_HINT = "Radius around Z0 such, that no other special point is in. But downt set it too small, as Function is supposed to blow to infinity at Z0, which may lead to calculation errors.";

	private static final int PRECISION = 5;

	public static void main(String[] args) {
		new AppFrame();
	}

	@SuppressWarnings("unused")
	private JLabel functionLabel;
	@SuppressWarnings("unused")
	private JLabel z0Label;
	@SuppressWarnings("unused")
	private JLabel r0Label;
	@SuppressWarnings("unused")
	private JLabel resLabel;

	private JTextField functionField;
	private JTextField z0Field;
	private JTextField r0Field;
	private JLabel resValueLabel;

	private JButton startButton;
	private JProgressBar progressBar;

	public AppFrame() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setupLayout();
		this.setVisible(true);

	}

	private void setupLayout() {
		this.setMinimumSize(new Dimension(500, 300));
		this.setTitle(APP_NAME);
		Container cp = this.getContentPane();
		cp.setLayout(new GridLayout(5, 2));
		cp.add(functionLabel = new JLabel("Function"));
		cp.add(functionField = new JTextField("1/z"));
		functionField.setToolTipText(FUNCTION_HINT);
		cp.add(z0Label = new JLabel("Z0"));
		cp.add(z0Field = new JTextField("0"));
		z0Field.setToolTipText(Z0_HINT);
		cp.add(r0Label = new JLabel("R0"));
		cp.add(r0Field = new JTextField("1"));
		r0Field.setToolTipText(R0_HINT);

		cp.add(startButton = new JButton("Evaluate"));
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread(() -> AppFrame.this.evaluate());
				t.setDaemon(true);
				t.setName("Calculation-Thread");
				t.start();
			}
		});
		cp.add(progressBar = new JProgressBar(SwingConstants.HORIZONTAL, (int) Math.pow(10, PRECISION)));
		progressBar.setStringPainted(true);

		cp.add(resLabel = new JLabel("Result:"));
		cp.add(resValueLabel = new JLabel("NaN+NaNi"));

	}

	private void evaluate() {
		System.out.println("Evaluating!");
		functionField.setEnabled(false);
		z0Field.setEnabled(false);
		r0Field.setEnabled(false);
		startButton.setEnabled(false);
		try {
			BracerParser bp = new BracerParser(PRECISION + 1);
			double r0 = Double.parseDouble(r0Field.getText());
			bp.parse(z0Field.getText());
			Complex z0 = bp.evaluateComplex();
			String function = functionField.getText();
			function = String.format("(%s)*exp(I*z)", function.replace("z", String.format("(%s%sI+%s*exp(I*z))",
					z0.getReal(), (z0.getImaginary() >= 0) ? "+" + z0.getImaginary() : z0.getImaginary(), r0)));
			System.out.println(function);
			bp.parse(function);

			ComplexNumber res = new ComplexNumber();
			int steps = (int) Math.pow(10, PRECISION);
			double step = 2.0 * Math.PI / steps;
			Complex tmp;
			for (int i = 0; i < steps; i++) {
				tmp = bp.evaluateComplex(step * i);
				res.add(tmp.getReal(), tmp.getImaginary());
				progressBar.setValue(i);
//				System.out.println(String.format("At %f got sum %s", i * step, res));
			}
			progressBar.setValue(steps);
			res.multiply(r0 / steps);
			resValueLabel.setText(res.toString());
		} catch (ParseException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Please, check you input syntax", "Error", JOptionPane.WARNING_MESSAGE);
		}
		functionField.setEnabled(true);
		z0Field.setEnabled(true);
		r0Field.setEnabled(true);
		startButton.setEnabled(true);
	}

}
