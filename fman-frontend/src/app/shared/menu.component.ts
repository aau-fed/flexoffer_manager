import { Component, Input } from '@angular/core';
import { Menu } from './models/menu.model';
@Component({
  selector: 'menu',
  templateUrl: './menu.component.html',
  styleUrls: []
})
export class MenuComponent{

   menus: Menu[];

  constructor() {
    this.menus = [
        {
          TITLE:'Menu 1',
          URL:'#',
          CHILDREN:[
            {
              TITLE:'Menu1_1',
              URL:'#',
              CHILDREN:[],
            },
            {
              TITLE:'Menu1_2',
              URL:'#',
              CHILDREN:[],
            }
          ],
        },
        {
          TITLE:'Menu 2',
          URL:'#',
          CHILDREN:[
            {
              TITLE:'Menu 2_1',
              URL:'#',
              CHILDREN:[],
            },
            {
              TITLE:'Menu2_2',
              URL:'#',
              CHILDREN:[],
            }
          ],
        }
    ];
   }


}
