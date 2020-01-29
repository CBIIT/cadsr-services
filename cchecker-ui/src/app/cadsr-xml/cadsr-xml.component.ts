import { Component, OnInit } from '@angular/core';
import { XmlService } from '../services/xml.service';
import { RestService } from '../services/rest.service';

@Component({
  selector: 'app-cadsr-xml',
  templateUrl: './cadsr-xml.component.html',
  styleUrls: ['./cadsr-xml.component.css']
})
export class CadsrXmlComponent implements OnInit {
  xmlContexts:Object;
  constructor(private xmlService: XmlService, private restService: RestService) { }

  ngOnInit() {
    this.restService.getXmlContexts().subscribe(
      data => {
        this.xmlContexts = data;
      },
      error => {
        this.xmlContexts = ["ABTC", "AECC", "AHRQ", "Alliance", "BBRB", "BOLD", "BRIDG"]
      },
      () => {
      }
    );
  }

}
