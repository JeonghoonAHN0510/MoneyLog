import { Dispatch, SetStateAction } from 'react';

export const createDialogOpenChangeHandler = (
    setOpen: Dispatch<SetStateAction<boolean>>,
    onClose?: () => void
) => {
    return (open: boolean) => {
        setOpen(open);
        if (!open) {
            onClose?.();
        }
    };
};
