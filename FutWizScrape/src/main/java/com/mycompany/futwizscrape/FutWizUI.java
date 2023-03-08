/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.futwizscrape;

import com.mycompany.futwizscrape.FutWizScrape.*;
import com.mycompany.futwizscrape.FutWizScrape.PlayerSearchTask*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author teddy
 */
public class FutWizUI extends javax.swing.JFrame {

    private JTextField playerNameTextField;
    private JTextField playerRatingTextField;
    private JTextField playerTypeTextField;
    private JTextArea playerDetailsTextArea;

    public FutWizUI() {
        super("FutWizScrape");

        playerNameTextField = new JTextField();
        playerRatingTextField = new JTextField();
        playerTypeTextField = new JTextField();
        playerDetailsTextArea = new JTextArea();

        // set up the layout
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Player Name:"));
        inputPanel.add(playerNameTextField);
        inputPanel.add(new JLabel("Player Rating:"));
        inputPanel.add(playerRatingTextField);
        inputPanel.add(new JLabel("Player Type:"));
        inputPanel.add(playerTypeTextField);
        add(inputPanel, BorderLayout.NORTH);

        JScrollPane outputScrollPane = new JScrollPane(playerDetailsTextArea);
        add(outputScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = playerNameTextField.getText().toLowerCase();
                String playerRating = playerRatingTextField.getText().toLowerCase();
                String playerType = playerTypeTextField.getText().toLowerCase();
                playerDetailsTextArea.setText("");
                if (playerName.isEmpty() || playerRating.isEmpty() || playerType.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields");
                    return;
                }
                playerDetailsTextArea.setText("Searching...\n");

                ExecutorService executor = Executors.newFixedThreadPool(1);
                Future<?> searchTask = executor.submit(new PlayerSearchTask(playerName, playerRating, playerType, 1, 800, new Scanner(System.in)));
                executor.shutdown();

                try {
                    searchTask.get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }

                if (KEEP_SEARCHING.get()) {
                    PlayerDetailsTextArea.append("No results found.");
                } else {
                    PlayerDetailsTextArea.append("Search complete.");
                }
            }
        });
        buttonPanel.add(searchButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 540, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 339, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FutWizUI futWizUI = new FutWizUI();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
