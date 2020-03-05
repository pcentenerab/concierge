import React, { Component } from 'react';
import GridComponent from './GridComponent';
import {roomservices} from '../constants/roomservices'

class RoomServicePage extends Component {
    render() {
        return (
            <GridComponent data={roomservices}/>
        );
    }
}

export default RoomServicePage;