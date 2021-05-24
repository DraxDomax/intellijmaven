package com.mycompany.qa.httptesting.soaptest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;

public class ParseXml {
    public static String changeUniqueTagValue (String xmlToUpdate, String tagToUpdate, String newValue) {
        try {
            // TODO: Can handle this (and getTagFirstValue) a little more professionally:
            // TODO: Can solve non-uniqueness functionality by iterating this guy:
            String[] slices = xmlToUpdate.split("<" + tagToUpdate + ">");
            if (slices.length == 2) {
                String beforeTag = slices[0];
                String closeTag = "/" + cleanTag(tagToUpdate);

                String[] afterCloseOfCurrentTagParts = slices[1].split(closeTag + ">");
                StringBuilder afterTagTemp = new StringBuilder();
                for (int i=1; i < afterCloseOfCurrentTagParts.length; i++) {
                    afterTagTemp.append(closeTag + ">");
                    afterTagTemp.append(afterCloseOfCurrentTagParts[i]);
                }
                String afterTag = afterTagTemp.toString();

                String newXML = beforeTag + "<" + tagToUpdate + ">" + newValue + "<" +afterTag;
                return newXML;
            } else {
                throw new RuntimeException("Could not find tag or tag is not unique: " + tagToUpdate);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to change value (existing? Unique?) of: " + tagToUpdate, e);
        }
    }

    public static String getTagFirstValue (String xmlToParse, String tagToFind) {
        String[] slices = xmlToParse.split("<"+tagToFind+">");
        if (slices.length >= 2) {
            String valueAndAfter = slices[1];
            return valueAndAfter.split("</" + tagToFind + ">")[0];
        } else {
            throw new RuntimeException("Could not find tag: " + tagToFind);
        }
    }

    public static String getTagAttribute (String xmlToParse, String tagToFind, String attribute) {
        String[] afterTagSplit = xmlToParse.split("<"+tagToFind+" ");
        if (afterTagSplit.length > 1) {
            String untilEngOfTag = afterTagSplit[1].split(">")[0];
            String afterAttributeName = untilEngOfTag.split(attribute)[1];
            String valueAssignment = afterAttributeName.split(" ")[0];
            return valueAssignment.split("\"")[1];
        } else {
            return "tagNotFound";
        }
    }

    public static String getTagFirstValue_ignoreAttribute (String xmlToParse, String tagToFind) {
        String[] slices = xmlToParse.split("<"+tagToFind+"[ ,>]");
        if (slices.length >= 2) {
            String valueAndAfter = slices[1];
            return valueAndAfter.split("</" + cleanTag(tagToFind) + ">")[0];
        } else {
            throw new RuntimeException("Could not find tag: " + tagToFind);
        }
    }

    public static String changeTagAttribute (String xmlToParse, String attributeToChg, String newValue) {
        try {
            String[] beforeAndafterAtt = xmlToParse.split(attributeToChg+"=\"");
            String beforeAttKeyandValueStart = beforeAndafterAtt[0];
            String afterAttKeyandValueStart = beforeAndafterAtt[1];

            String[] allQuoteParts = afterAttKeyandValueStart.split("\"");
            StringBuilder afterAttValueEndTemp = new StringBuilder();
            for (int i=1; i < allQuoteParts.length; i++) {
                afterAttValueEndTemp.append("\"");
                afterAttValueEndTemp.append(allQuoteParts[i]);
            }
            String afterAttValueEnd = afterAttValueEndTemp.toString();

            return beforeAttKeyandValueStart + attributeToChg + "=\"" + newValue + afterAttValueEnd;
        } catch (Exception e) {
            throw new RuntimeException("Could not find attribute or attribute is not unique: " + attributeToChg);
        }
    }

    // Remove attributes, if any:
    private static String cleanTag (String tagName) {
        return tagName.split(" ")[0];
    }

    public static String createTag (String tagName, String tagText) {
        String openTag = "<" + tagName + ">";
        String closeTag = "</" + tagName + ">";
        return openTag+tagText+closeTag;
    }

    public static String[] getRepeatingTagValues (String sourceXml, String tagOfInterest) {
        String tag = "<" + tagOfInterest + ">";
        String closingTag = "</" + tagOfInterest + ">";

        String[] choppedData = sourceXml.split(tag);
        int choppedDataLength = choppedData.length;

        if (choppedDataLength == 1) {
            System.out.println("***Tag not found in data for getRepeatingTagValues***");
            return null;
        } else {
            String[] values = new String[choppedDataLength-1];
            for (int i=1;i<choppedDataLength;i++) {
                String currentValue = choppedData[i].split(closingTag)[0];
                values[i-1]=currentValue;
            }
            return values;
        }
    }

    public static String insertXmlAfterUniqueTag (String recipientXml, String newXml, String afterThisTag) {
        String afterThisTagClose = "</"+afterThisTag+">";
        String[] parts = recipientXml.split(afterThisTagClose);
        return parts[0]+afterThisTagClose+newXml+parts[1];
    }

    public static String createRepeatingTags (String newTag, String[] values) {
        String tag = "<" + newTag + ">";
        String closingTag = "</" + newTag + ">";
        StringBuilder result = new StringBuilder();
        for (String currentValue : values) {
            result.append(tag+currentValue+closingTag+"\n");
        }
        return result.toString();
    }

    public static String removeTagAndContents (String fullXml, String tagToDelete) {
        String wipXml = changeUniqueTagValue(fullXml,tagToDelete,"");
        String tagOpen = "<"+tagToDelete+">";
        String tagClose = "</"+tagToDelete+">";
        wipXml = wipXml.replace(tagOpen, "");
        wipXml = wipXml.replace(tagClose, "");
        return wipXml;
    }

    public static String addAttributeToUniqueTag (String oldXml, String tagToAddTo, String attribute, String attValue) {
        // TODO: consider if there are other attributes already...
        String tagToAddToXml = "<"+tagToAddTo+">";
        String parts[] = oldXml.split(tagToAddToXml);
        return parts[0] + "<" + tagToAddTo + " " + attribute + "=\"" + attValue + "\"" + ">" + parts[1];
    }

    public static String changeTagValueAtPath (String unmodifiedXml, String xPath, String newValue) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(unmodifiedXml));
            document = documentBuilder.parse(inputSource);
        } catch (Exception e) {
            throw new RuntimeException("Failed instantiating document builder or parsing input source", e);
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        Element element;
        try {
            element = (Element) xpath.evaluate(xPath, document, XPathConstants.NODE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate xPath to an element", e);
        }
        element.setTextContent(newValue);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        StringWriter outWriter;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            outWriter = new StringWriter();
            StreamResult resultStream = new StreamResult(outWriter);
            transformer.transform(domSource, resultStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate transformer or to transform the source document", e);
        }

        StringBuffer stringBuffer = outWriter.getBuffer();
        return stringBuffer.toString();
    }

    public static String readTagValueAtPath (String sourceXml, String xPath) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(sourceXml));
            document = documentBuilder.parse(inputSource);
        } catch (Exception e) {
            throw new RuntimeException("Failed instantiating document builder or parsing input source", e);
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        Element element;
        try {
            element = (Element) xpath.evaluate(xPath, document, XPathConstants.NODE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate xPath to an element", e);
        }
        return element.getTextContent();
    }

    public static String docToString (Document document) {
        try {
            StringWriter stringWriter = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error converting to String", e);
        }
    }
}
