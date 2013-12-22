package com.pgore.options;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class CommandLineOptions {
    protected Option httpURL;
    protected Option localFile;
    protected Option help;

    protected CommandLine cl;
    protected HelpFormatter helpFormatter;
    protected GroupBuilder groupBuilder = new GroupBuilder().withName("options");
    protected boolean helpFlag;

    protected static CommandLineOptions staticCommandLineOptions = null;

    public CommandLineOptions(String[] args) throws OptionException {
        parseArgs(args);
    }

    public void parseArgs(String args[]) throws OptionException {
        buildCoreOptions(); //builds the core options.
        Parser parser = new Parser();
        Group options = groupBuilder.create();
        parser.setGroup(options);

        helpFormatter = new HelpFormatter();
        helpFormatter.setGroup(options);
        helpFlag = false;

        cl = parser.parseAndHelp(args);
        //Hack so that application can print help and exit
        if(cl!=null){
            helpFlag = cl.hasOption(help);
        }else if(args.length==1 &&
                (args[0].equals("-h") || args[0].equals("--help"))){
            helpFlag = true;
        }else{
            throw new IllegalArgumentException("Wrong commandline options. Check Usage");
        }
    }

    public static synchronized CommandLineOptions getCommandlineOptions(){
        return staticCommandLineOptions;
    }

    public void printHelpString(){
        helpFormatter.print();
    }

    public URL getHttpURL(){
        if(cl==null)
            return null;
        return (URL) cl.getValue(this.httpURL);
    }

    public String getFilePath(){
        if(cl == null)
            return null;
        else return (String) cl.getValue(this.localFile);
    }

    /**
     * @return boolean indicating if "-h" option is a part of the args
     */
    public boolean getHelpSwitch(){
        return helpFlag;
    }

    private final void buildCoreOptions(){
        DefaultOptionBuilder defaultOptionBuilder = new DefaultOptionBuilder();
        ArgumentBuilder argumentBuilder = new ArgumentBuilder();
        help =
                defaultOptionBuilder
                        .withLongName("help")
                        .withShortName("h")
                        .withDescription("print this message")
                        .create();
        UriValidator validator = new UriValidator();
        validator.addAcceptableScheme("https");
        validator.addAcceptableScheme("http");
        httpURL =
                defaultOptionBuilder
                        .withLongName("aws-url")
                        .withShortName("u")
                        .withDescription("AWS URL that you hold")
                        .withRequired(true) //Argument is Mandatory !!
                        .withArgument(
                                argumentBuilder
                                        .withName("AWS URL")
                                        .withMinimum(1)
                                        .withMaximum(1)
                                        .withValidator(validator)
                                        .create())
                        .create();
        FileValidator fileValidator = new FileValidator();
        localFile =
                defaultOptionBuilder
                        .withLongName("file-path")
                        .withShortName("f")
                        .withDescription("File that you with to upload")
                        .withRequired(true) //Argument is Mandatory !!
                        .withArgument(
                                argumentBuilder
                                        .withName("Local file")
                                        .withMinimum(1)
                                        .withMaximum(1)
                                        .withValidator(fileValidator)
                                        .create())
                        .create();
        groupBuilder = groupBuilder
                .withOption(httpURL)
                .withOption(localFile)
                .withOption(help);
    }

    private class UriValidator implements Validator {
        private List<String> acceptableSchemes = new ArrayList<String>();
        private String acceptableSchemesRep = "";

        public void addAcceptableScheme(String scheme){
            acceptableSchemes.add(scheme);
            if(acceptableSchemesRep.isEmpty()){
                acceptableSchemesRep = acceptableSchemesRep.concat(scheme);
            }
            else{
                acceptableSchemesRep = acceptableSchemesRep.concat(", " + scheme);
            }
        }

        @Override
        public void validate(List list) throws InvalidArgumentException {
            for (final ListIterator i = list.listIterator(); i.hasNext();) {
                final String value = (String) i.next();
                String arg = value.toString();
                URL url=null;
                try {
                    url = new URL(arg);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if(!acceptableSchemes.isEmpty()){
                    if(acceptableSchemes.contains(url.getProtocol())){
                        i.set(url);
                    }
                    else{
                        throw new InvalidArgumentException("Illegal scheme: "+ url.getProtocol() +
                                " for URL - " + arg+ ". Should be one of [" + acceptableSchemesRep+ "]");
                    }
                }else{
                    i.set(url);
                }
            }
        }
    }


    private class FileValidator implements Validator {
        @Override
        public void validate(List list) throws InvalidArgumentException {
            for (final ListIterator i = list.listIterator(); i.hasNext();) {
                final String value = (String) i.next();
                String arg = value.toString();
                Path file = Paths.get(arg);
                if(!Files.exists(file)|| Files.isDirectory(file)){
                    throw new InvalidArgumentException("Invalid file value. please enter a valid, existent file path");
                }

            }
        }
    }
}

