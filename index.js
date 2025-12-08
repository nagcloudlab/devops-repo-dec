


function getCoffeeOrder(coffeeType, size) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            if (coffeeType && size) {
                resolve(`Order received: A ${size} ${coffeeType}`);
            } else {
                reject('Error: Missing coffee type or size');
            }
        }, 5000);
    });
    return orderDetails;
}

function eve_break() { 
    console.log("Eve is taking a break.");
    getCoffeeOrder('Latte', 'Large')
        .then(orderDetails => {
            console.log(orderDetails);
        })
        .catch(error => {
            console.error(error);
        });
}

eve_break();