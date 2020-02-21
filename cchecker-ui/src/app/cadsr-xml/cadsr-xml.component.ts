import { Component, OnInit } from '@angular/core';
import { RestService } from '../services/rest.service';
import { FormListService } from '../services/formlist.service';
import { saveAs } from 'file-saver'
import { Router } from '../../../node_modules/@angular/router';

@Component({
  selector: 'app-cadsr-xml',
  templateUrl: './cadsr-xml.component.html',
  styleUrls: ['./cadsr-xml.component.css']
})
export class CadsrXmlComponent implements OnInit {
  xmlContexts:Object;
  statusMessage: String;
  isGeneratingXML: Boolean;
  loaded: Boolean;
  isValidating:Boolean;
  constructor(private restService: RestService, private formListServce:FormListService, private router:Router) { }

  formServiceClick = function(c) {
    this.statusMessage = null;
    this.isGeneratingXML = true;
    this.formListServce.setSessionDataItem('isGeneratingXML', true);
    const checkedItems = this.formListServce.getFormListAsSimpleArray();
    const sessionId = this.formListServce.getSessionDataItem('formListData')['sessionid'];
    this.restService.formXmlService(checkedItems, c, sessionId).subscribe(
      data => {
        console.log("data?")

      },
      error => {
        console.log("ERRR?")

        
      },
      () => {
        console.log("I AM DONE")
        this.downloadXmlFile(sessionId);
        const currentRoute = this.router.url;
        console.log(currentRoute);
        this.router.navigateByUrl('/generatexml')

        this.isGeneratingXML = false;
        this.formListServce.setSessionDataItem('isGeneratingXML', false);
      }
    );
  };

  downloadXmlFile = (sessionId) => {
    var that = this;
    this.restService.getXmlFileFromSession(sessionId).subscribe(
      data => {
        const filename = data.headers.get('Content-Disposition').replace('attachment; filename=', '')
        var blob = new Blob([data.body], { type: "application/xml" });
        saveAs(blob, filename);
      },
      error => {
        this.isGeneratingXML = false;
        this.formListServce.setSessionDataItem('isGeneratingXML', false);
        // this.errorMessage = 'Unexpected error, please contact Application Support (<a href="mailto:NCIAppSupport@nih.gov">NCIAppSupport@nih.gov</a>)';
        // const reader: FileReader = new FileReader();
        // reader.readAsText(error.error)
        // reader.onloadend = (error): void => this.errorMessage = reader.result;
      },
      () => {
        this.statusMessage = 'XML file downloaded succesfully';
        this.isGeneratingXML = false;
        this.formListServce.setSessionDataItem('isGeneratingXML',false);

      });



  };

  ngOnInit() {
    this.isValidating = this.formListServce.getSessionDataItem('isValidating');
    this.isGeneratingXML = this.formListServce.getSessionDataItem('isGeneratingXML');
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
