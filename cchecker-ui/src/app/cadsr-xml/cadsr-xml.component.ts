import { Component, OnInit } from '@angular/core';
import { XmlService } from '../services/xml.service';
import { RestService } from '../services/rest.service';
import { FormListService } from '../services/formlist.service';
import { saveAs } from 'file-saver'

@Component({
  selector: 'app-cadsr-xml',
  templateUrl: './cadsr-xml.component.html',
  styleUrls: ['./cadsr-xml.component.css']
})
export class CadsrXmlComponent implements OnInit {
  xmlContexts:Object;
  constructor(private xmlService: XmlService, private restService: RestService, private formListServce:FormListService) { }

  formServiceClick = function(c) {
    const checkedItems = this.formListServce.getFormListAsSimpleArray();
    const sessionId = this.formListServce.getSessionDataItem('formListData')['sessionid'];
    this.restService.formXmlService(checkedItems, c, sessionId).subscribe(
      data => {
      },
      error => {
        
      },
      () => {
        this.downloadXmlFile(sessionId);
      }
    );
  };

  downloadXmlFile = (sessionId) => {
    console.log(sessionId)
    var that = this;
    this.restService.getXmlFileFromSession(sessionId).subscribe(
      data => {
        const filename = data.headers.get('Content-Disposition').replace('attachment; filename=', '')
        var blob = new Blob([data.body], { type: "application/xml" });
        saveAs(blob, filename);
      },
      error => {
        // this.isGenerating = false;
        // this.errorMessage = 'Unexpected error, please contact Application Support (<a href="mailto:NCIAppSupport@nih.gov">NCIAppSupport@nih.gov</a>)';
        // const reader: FileReader = new FileReader();
        // reader.readAsText(error.error)
        // reader.onloadend = (error): void => this.errorMessage = reader.result;
      },
      () => {
        // that.isGenerating = false;
        console.log("DONE")
      });



  };

  ngOnInit() {
    this.restService.getXmlContexts().subscribe(
      data => {
        this.xmlContexts = data;
      },
      error => {
        this.xmlContexts = []
      },
      () => {
      }
    );
  }

}
