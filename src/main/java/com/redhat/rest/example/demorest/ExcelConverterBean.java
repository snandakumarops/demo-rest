package com.redhat.rest.example.demorest;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import org.apache.camel.Body;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.springframework.stereotype.Component;

/**
 * Uses POI to convert an Excel spreadsheet to the desired JAXB XML format.
 */

public class ExcelConverterBean {
    private final static Log log = LogFactory.getLog(ExcelConverterBean.class);

    public List<CaseData> process(@Body InputStream body) {
        List<CaseData> caseDetails = new ArrayList<CaseData>();
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(body);
            HSSFSheet sheet = workbook.getSheetAt(0);
            DatatypeFactory dateFactory = DatatypeFactory.newInstance();
            boolean headersFound = false;
            int colNum;
            for(Iterator rit = sheet.rowIterator(); rit.hasNext();) {
                HSSFRow row = (HSSFRow) rit.next();
                if(!headersFound) {  // Skip the first row with column headers
                    headersFound = true;
                    continue;
                }
                colNum = 0;
                for(Iterator cit = row.cellIterator(); cit.hasNext(); ++colNum) {
                    CaseData caseData = new CaseData();
                    HSSFCell cell = (HSSFCell) cit.next();
                    if(headersFound)
                        switch(colNum) {
                            case 0: // customerName
                                caseData.setCustomerAccNo(cell.getStringCellValue());
                                break;
                            case 1: // customerPhone
                                caseData.setCustomerPhone(cell.getStringCellValue());
                            case 2: // customerAddress
                               caseData.setCustomerAddress(cell.getStringCellValue());
                               break;
                            case 3: // complaintsDescription
                                caseData.setComplaintsDescription(cell.getStringCellValue());
                                break;
                            case 4: // category
                                caseData.setCategory(cell.getStringCellValue());
                                break;
                            case 5: // businessUnit
                                caseData.setBusinessUnit(cell.getStringCellValue());
                                break;

                        }
                    caseDetails.add(caseData);
                }

            }
        } catch (Exception e) {
            log.error("Unable to import Excel invoice", e);
            throw new RuntimeException("Unable to import Excel invoice", e);
        }
        return caseDetails;
    }
}