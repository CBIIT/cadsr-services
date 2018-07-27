import { Component, OnInit, Input } from '@angular/core';
import { KeyRegistry } from '../../../node_modules/@angular/core/src/di/reflective_key';
@Component({
  selector: 'app-progress-bar',
  templateUrl: './progress-bar.component.html',
  styleUrls: ['./progress-bar.component.css']
})
export class ProgressBarComponent implements OnInit {
  @Input() percentage:Number;

  constructor()   {
  }

  getProgress = () => this.percentage<100 ? `Uploading`:'Complete';

  ngOnInit() {
  }
}
